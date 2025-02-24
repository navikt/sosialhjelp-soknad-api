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
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
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
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.ByteArrayInputStream
import java.util.Collections
import java.util.concurrent.Future

interface MellomlagringClient {
    fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto?

    fun lastOppDokument(
        navEksternId: String,
        filOpplasting: FilOpplasting,
    ): MellomlagringDto

    fun lastOppDokument(
        navEksternId: String,
        filnavn: String,
        data: ByteArray,
    ): MellomlagringDto

    fun slettAlleDokumenter(navEksternId: String)

    fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray

    fun slettDokument(
        navEksternId: String,
        digisosDokumentId: String,
    )
}

class MellomlagringClientImpl(
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val maskinportenClient: MaskinportenClient,
    private val webClient: WebClient,
) : MellomlagringClient {
    override fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto? {
        return try {
            webClient.get()
                .uri(MELLOMLAGRING_PATH, navEksternId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
                .retrieve()
                .bodyToMono<MellomlagringDto>()
                .block() ?: throw FiksException("MellomlagringDto er null?", null)
        } catch (e: WebClientResponseException) {
            if (e is BadRequest || e is NotFound) {
                val errorMessage = digisosObjectMapper.readValue<ErrorMessage>(e.responseBodyAsString)
                val message = errorMessage.message
                if (message != null && message.contains("Fant ingen data i basen knytter til angitt id'en")) {
                    return null
                }
            }
            log.warn("Fiks - getMellomlagredeVedlegg feilet - ${e.responseBodyAsString}", e)
            throw e
        }
    }

    override fun lastOppDokument(
        navEksternId: String,
        filnavn: String,
        data: ByteArray,
    ): MellomlagringDto {
        return lastOppDokument(
            navEksternId = navEksternId,
            filOpplasting =
                FilOpplasting(
                    metadata =
                        FilMetadata(
                            filnavn = filnavn,
                            mimetype = "application/octet-stream",
                            storrelse = data.size.toLong(),
                        ),
                    data = ByteArrayInputStream(data),
                ),
        )
    }

    /**
     * Last opp vedlegg til mellomlagring for `navEksternId`
     */
    override fun lastOppDokument(
        navEksternId: String,
        filOpplasting: FilOpplasting,
    ): MellomlagringDto {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(1))
        val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()

        return runCatching {
            doUploadDocument(
                navEksternId = navEksternId,
                filForOpplasting =
                    FilForOpplasting(
                        filnavn = filOpplasting.metadata.filnavn,
                        metadata = filOpplasting.metadata,
                        data = krypteringService.krypter(filOpplasting.data, krypteringFutureList, fiksX509Certificate),
                    ),
            )
                .also { waitForFutures(krypteringFutureList) }
        }
            .onFailure { log.error("Feil ved opplasting av dokument", it) }
            .getOrElse { throw FiksException("Feil ved opplasting av dokument", it) }
            .also {
                krypteringFutureList
                    .filter { !it.isDone && !it.isCancelled }
                    .forEach { it.cancel(true) }
            }
    }

    private fun doUploadDocument(
        filForOpplasting: FilForOpplasting<Any>,
        navEksternId: String,
    ): MellomlagringDto {
        val body = LinkedMultiValueMap<String, Any>()
        body.add("metadata", createHttpEntityOfString(getJson(filForOpplasting), "metadata"))
        body.add(filForOpplasting.filnavn, createHttpEntityOfFile(filForOpplasting, filForOpplasting.filnavn))

        val startTime = System.currentTimeMillis()
        return webClient.post()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono<MellomlagringDto>()
            .doOnSuccess {
                log.info("Mellomlagring av vedlegg ${filForOpplasting.metadata} utført.")
            }
            .doOnError(WebClientResponseException::class.java) {
                log.warn(
                    "Mellomlagring av vedlegg til søknad $navEksternId feilet etter ${System.currentTimeMillis() - startTime} ms med status ${it.statusCode} og response: ${it.responseBodyAsString}",
                    it,
                )
            }
            .block()
            ?: throw FiksException("MellomlagringDto er null ved opplasting av dokument", null)
    }

    /**
     * Slett alle mellomlagrede vedlegg for `navEksternId`
     */
    override fun slettAlleDokumenter(navEksternId: String) {
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
    override fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray {
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
    override fun slettDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ) {
        webClient.delete()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .doOnSuccess {
                log.info("Fiks - delete mellomlagretVedlegg OK. vedleggId=$digisosDokumentId, behandlingsId=$navEksternId")
            }
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
            }
            .block()
    }

    private fun createHttpEntityOfString(
        body: String,
        name: String,
    ): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "text/plain;charset=UTF-8")
    }

    private fun createHttpEntityOfFile(
        file: FilForOpplasting<Any>,
        name: String,
    ): HttpEntity<Any> {
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
