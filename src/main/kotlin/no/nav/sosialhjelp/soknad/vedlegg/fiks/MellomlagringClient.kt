package no.nav.sosialhjelp.soknad.vedlegg.fiks

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import io.netty.channel.ChannelOption
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.Collections
import java.util.concurrent.Future

@Component
class MellomlagringClient(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val maskinportenClient: MaskinportenClient,
    proxiedWebClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {

    private val webClient = proxiedWebClientBuilder
        .baseUrl(digisosApiEndpoint)
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .build()

    /**
     * Hent metadata om alle mellomlagret vedlegg for `navEksternId`
     */
    fun getMellomlagredeVedlegg(navEksternId: String): MellomlagringDto? {
        val responseString: String
        try {
            responseString = webClient.get()
                .uri(MELLOMLAGRING_PATH, navEksternId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
                .retrieve()
                .bodyToMono<String>()
                .onErrorMap(WebClientResponseException::class.java) {
                    log.warn("Fiks - getMellomlagredeVedlegg feilet - ${it.responseBodyAsString}", it)
                    throw it
                }
                .block() ?: throw FiksException("MellomlagringDto er null?", null)
            log.info("Response: $responseString")
        } catch (badRequest: WebClientResponseException.BadRequest) {
            val errorMessage = digisosObjectMapper.readValue<ErrorMessage>(badRequest.responseBodyAsString)
            if (errorMessage.message == "Fant ingen data i basen knytter til angitt id'en") {
                log.info("Ingen mellomlagrede vedlegg funnet")
                return null
            }
            throw badRequest
        }
        return digisosObjectMapper.readValue<MellomlagringDto>(responseString)
    }

    /**
     * Last opp vedlegg til mellomlagring for `navEksternId`
     */
    fun postVedlegg(navEksternId: String, filOpplasting: FilOpplasting) {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(1))

        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
            lastOpp(
                filForOpplasting = FilForOpplasting.builder<Any>()
                    .filnavn(filOpplasting.metadata.filnavn)
                    .metadata(filOpplasting.metadata)
                    .data(krypteringService.krypter(filOpplasting.data, krypteringFutureList, fiksX509Certificate))
                    .build(),
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
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Mellomlagring av vedlegg til søknad $navEksternId feilet etter ${System.currentTimeMillis() - startTime} ms med status ${it.statusCode} og response: ${it.responseBodyAsString}")
            }
            .block()
    }

    /**
     * Slett alle mellomlagrede vedlegg for `navEksternId`
     */
    fun deleteAllVedlegg(navEksternId: String) {
        webClient.delete()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap(WebClientResponseException::class.java) {
                log.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
                throw it
            }
            .block()
    }

    /**
     * Last ned mellomlagret vedlegg
     */
    fun getVedlegg(navEksternId: String, digisosDokumentId: String): ByteArray {
        return webClient.get()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .block()
            ?.let { digisosObjectMapper.writeValueAsBytes(it) }
            ?: throw FiksException("Mellomlagret vedlegg er null?", null)
    }

    /**
     * Slett mellomlagret vedlegg
     */
    fun deleteVedlegg(navEksternId: String, digisosDokumentId: String) {
        webClient.delete()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap(WebClientResponseException::class.java) {
                log.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
                throw it
            }
            .block()
    }

    private fun createHttpEntityOfString(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "text/plain;charset=UTF-8")
    }

    private fun createHttpEntityOfFile(file: FilForOpplasting<Any>, name: String): HttpEntity<Any> {
        return createHttpEntity(InputStreamResource(file.data), name, file.filnavn, "application/octet-stream")
    }

    private fun createHttpEntity(body: Any, name: String, filename: String?, contentType: String): HttpEntity<Any> {
        val headerMap = LinkedMultiValueMap<String, String>()
        val builder: ContentDisposition.Builder = ContentDisposition
            .builder("form-data")
            .name(name)
        val contentDisposition: ContentDisposition =
            if (filename == null) builder.build() else builder.filename(filename).build()

        headerMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        headerMap.add(HttpHeaders.CONTENT_TYPE, contentType)
        return HttpEntity(body, headerMap)
    }

    companion object {
        private const val MELLOMLAGRING_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}"
        private const val MELLOMLAGRING_DOKUMENT_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}"

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter

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
