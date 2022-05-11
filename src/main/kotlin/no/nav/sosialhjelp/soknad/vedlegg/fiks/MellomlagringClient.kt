package no.nav.sosialhjelp.soknad.vedlegg.fiks

import com.fasterxml.jackson.core.JsonProcessingException
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksServiceUnavailableRetryStrategy
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.eclipse.jetty.http.HttpHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.Future

@Component
class MellomlagringClient(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    proxiedWebClientBuilder: WebClient.Builder
) {
    private val webClient = proxiedWebClientBuilder
        .baseUrl(digisosApiEndpoint)
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .build()

    private val retryHandler = DefaultHttpRequestRetryHandler()
    private val serviceUnavailableRetryStrategy = FiksServiceUnavailableRetryStrategy()

    private val requestConfig = RequestConfig.custom()
        .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .build()

    private val clientBuilder = HttpClientBuilder.create()
        .setRetryHandler(retryHandler)
        .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
        .useSystemProperties()
        .setDefaultRequestConfig(requestConfig)

    fun getMellomlagredeVedlegg(navEksternId: String, token: String): MellomlagringDto {
        return webClient.get()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono<MellomlagringDto>()
            .onErrorMap(WebClientResponseException::class.java) {
                log.warn("Fiks - getMellomlagredeVedlegg feilet - ${it.responseBodyAsString}", it)
                throw it
            }
            .block() ?: throw FiksException("MellomlagringDto er null?", null)
    }

    fun postVedlegg(navEksternId: String, filOpplasting: FilOpplasting, token: String) {
        log.info("start kryptering av fil")
        val krypteringFutureList = Collections.synchronizedList(mutableListOf<Future<Void>>())

        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
            lastopp(
                filForOpplasting = FilForOpplasting.builder<Any>()
                    .filnavn(filOpplasting.metadata.filnavn)
                    .metadata(filOpplasting.metadata)
                    .data(krypteringService.krypter(filOpplasting.data, krypteringFutureList, fiksX509Certificate))
                    .build(),
                navEksternId = navEksternId,
                token = token
            )
            waitForFutures(krypteringFutureList)
        } catch (e: Exception) {
            log.info("noe feil skjedde ved kryptering", e)
            throw e
        } finally {
            log.info("finally blokk")
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        log.info("slutt kryptering av fil")
    }

    private fun lastopp(filForOpplasting: FilForOpplasting<Any>, navEksternId: String, token: String) {
        val entitybuilder = MultipartEntityBuilder.create()
        entitybuilder.setCharset(StandardCharsets.UTF_8)
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)

        entitybuilder.addTextBody("metadata", getJson(filForOpplasting))
        entitybuilder.addBinaryBody(filForOpplasting.filnavn, filForOpplasting.data, ContentType.APPLICATION_OCTET_STREAM, filForOpplasting.filnavn)

        try {
            log.info("Starter post kall til KS mellomlagring - $digisosApiEndpoint/digisos/api/v1/mellomlagring/$navEksternId")
            clientBuilder.build().use { client ->
                val post = HttpPost("$digisosApiEndpoint/digisos/api/v1/mellomlagring/$navEksternId")
                // post.setHeader("requestid", UUID.randomUUID().toString())
                post.setHeader(HttpHeader.AUTHORIZATION.name, token)
                post.setHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
                post.setHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
                post.entity = entitybuilder.build()

                val startTime = System.currentTimeMillis()
                val response = client.execute(post)
                log.info("Response: ${response.statusLine.reasonPhrase}, ${digisosObjectMapper.writeValueAsString(response.entity.content.readBytes())}")
                val endTime = System.currentTimeMillis()
                if (response.statusLine.statusCode >= 300) {
                    val errorResponse = EntityUtils.toString(response.entity)
                    throw IllegalStateException(
                        "Mellomlagring av vedlegg til søknad $navEksternId feilet etter ${endTime - startTime} ms med status ${response.statusLine.reasonPhrase} og response: $errorResponse"
                    )
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Mellomlagring av vedlegg til søknad $navEksternId feilet", e)
        }
    }

    fun deleteAllVedleggFor(navEksternId: String, token: String) {
        // slett alle mellomlagrede dokument for søknad
    }

    fun getVedlegg(navEksternId: String, digisosDokumentId: String, token: String): ByteArray {
        // last ned mellomlagret dokument
        return webClient.get()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono<String>()
            .block()
            ?.let { digisosObjectMapper.writeValueAsBytes(it) }
            ?: throw FiksException("Mellomlagret vedlegg er null?", null)
    }

    fun deleteVedlegg(navEksternId: String, digisosDokumentId: String, token: String) {
        // slett mellomlagret dokument
        webClient.delete()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap(WebClientResponseException::class.java) {
                log.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
                throw it
            }
            .block()
    }

    companion object {
        private const val MELLOMLAGRING_PATH = "/digisos/api/v1/mellomlagring/{navEksternRefId}"
        private const val MELLOMLAGRING_DOKUMENT_PATH = "/digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}"

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter

        private val log by logger()

        private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String? {
            return try {
                digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
            } catch (e: JsonProcessingException) {
                throw IllegalStateException(e)
            }
        }
    }
}
