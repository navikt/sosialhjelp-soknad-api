package no.nav.sosialhjelp.soknad.vedlegg.fiks

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilForOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Collections
import java.util.concurrent.Future

interface MellomlagringClient {
    fun getMellomlagredeVedlegg(navEksternId: String): MellomlagringDto?
    fun postVedlegg(navEksternId: String, filOpplasting: FilOpplasting)
    fun deleteAllVedlegg(navEksternId: String)
    fun getVedlegg(navEksternId: String, digisosDokumentId: String): ByteArray
    fun deleteVedlegg(navEksternId: String, digisosDokumentId: String)
}

class MellomlagringClientImpl(
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val maskinportenClient: MaskinportenClient,
    private val webClient: WebClient
) : MellomlagringClient {

    /**
     * Hent metadata om alle mellomlagret vedlegg for `navEksternId`
     */
    override fun getMellomlagredeVedlegg(navEksternId: String): MellomlagringDto? {
        val responseString: String
        try {
            responseString = webClient.get()
                .uri(MELLOMLAGRING_PATH, navEksternId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
                .retrieve()
                .bodyToMono<String>()
                .block() ?: throw FiksException("MellomlagringDto er null?", null)
        } catch (e: WebClientResponseException) {
            if (e is BadRequest) {
                val errorMessage = digisosObjectMapper.readValue<ErrorMessage>(e.responseBodyAsString)
                val message = errorMessage.message
                if (message != null && message.contains("Fant ingen data i basen knytter til angitt id'en")) {
                    log.warn("Ingen mellomlagrede vedlegg funnet for $navEksternId")
                    return null
                }
            }
            log.warn("Fiks - getMellomlagredeVedlegg feilet - ${e.responseBodyAsString}", e)
            throw e
        }
        return digisosObjectMapper.readValue<MellomlagringDto>(responseString)
    }

    /**
     * Last opp vedlegg til mellomlagring for `navEksternId`
     */
    override fun postVedlegg(navEksternId: String, filOpplasting: FilOpplasting) {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(1))

        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
            lastOpp(
                filForOpplasting = FilForOpplasting(
                    filnavn = filOpplasting.metadata.filnavn,
                    metadata = filOpplasting.metadata,
                    data = krypteringService.krypter(filOpplasting.data, krypteringFutureList, fiksX509Certificate)
                ),
                navEksternId = navEksternId
            )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
    }

    private fun lastOpp(filForOpplasting: FilForOpplasting<Any>, navEksternId: String) {
        val body = LinkedMultiValueMap<String, Any>()
        body.add("metadata", createHttpEntityOfString(getJson(filForOpplasting), "metadata"))
        body.add(filForOpplasting.filnavn, createHttpEntityOfFile(filForOpplasting, filForOpplasting.filnavn))

        val startTime = System.currentTimeMillis()
        webClient.post()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono<String>()
            .doOnSuccess { log.info("Mellomlagring av vedlegg til søknad $navEksternId utført.")}
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Mellomlagring av vedlegg til søknad $navEksternId feilet etter ${System.currentTimeMillis() - startTime} ms med status ${it.statusCode} og response: ${it.responseBodyAsString}", it)
            }
            .block()
    }

    /**
     * Slett alle mellomlagrede vedlegg for `navEksternId`
     */
    override fun deleteAllVedlegg(navEksternId: String) {
        webClient.delete()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Fiks - deleteAll mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
            }
            .block()
    }

    /**
     * Last ned mellomlagret vedlegg
     */
    override fun getVedlegg(navEksternId: String, digisosDokumentId: String): ByteArray {
        return webClient.get()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<ByteArray>()
            .block()
            ?: throw FiksException("Mellomlagret vedlegg er null?", null)
    }

    /**
     * Slett mellomlagret vedlegg
     */
    override fun deleteVedlegg(navEksternId: String, digisosDokumentId: String) {
        webClient.delete()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
            }
            .block()
    }

    private fun createHttpEntityOfString(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "text/plain;charset=UTF-8")
    }

    private fun createHttpEntityOfFile(file: FilForOpplasting<Any>, name: String): HttpEntity<Any> {
        return createHttpEntity(ByteArrayResource(IOUtils.toByteArray(file.data)), name, file.filnavn, "application/octet-stream")
    }

    companion object {
        private const val MELLOMLAGRING_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}"
        private const val MELLOMLAGRING_DOKUMENT_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}"

        private val log by logger()

        private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
            return try {
                digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
            } catch (e: JsonProcessingException) {
                throw IllegalStateException(e)
            }
        }
    }
}
