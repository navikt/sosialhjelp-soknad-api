package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import io.netty.channel.ChannelOption
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.io.IOException
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future

interface DigisosApiV2Client {
    fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String
}

class DigisosApiV2ClientImpl(
    private val digisosApiEndpoint: String,
    integrasjonsidFiks: String,
    integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) : DigisosApiV2Client {

//    private val retryHandler = DefaultHttpRequestRetryHandler()
//    private val serviceUnavailableRetryStrategy = FiksServiceUnavailableRetryStrategy()

//    private val requestConfig = RequestConfig.custom()
//        .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .build()

//    private val clientBuilder = HttpClientBuilder.create()
//        .setRetryHandler(retryHandler)
//        .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
//        .setDefaultRequestConfig(requestConfig)
//        .useSystemProperties()

    private val fiksWebClient = webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(digisosObjectMapper))
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(digisosObjectMapper))
        }
        .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .build()

    override fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(dokumenter.size))
        val digisosId: String
        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
            digisosId = lastOppFiler(
                soknadJson,
                tilleggsinformasjonJson,
                vedleggJson,
                dokumenter.map { dokument: FilOpplasting ->
                    FilForOpplasting.builder<Any>()
                        .filnavn(dokument.metadata.filnavn)
                        .metadata(
                            FilMetadata(
                                filnavn = dokument.metadata.filnavn,
                                mimetype = dokument.metadata.mimetype,
                                storrelse = dokument.metadata.storrelse
                            )
                        )
                        .data(krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate))
                        .build()
                },
                kommunenr,
                navEksternRefId,
                token
            )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        return digisosId
    }

    private fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilForOpplasting<Any>>,
        kommunenummer: String,
        behandlingsId: String,
        token: String?
    ): String {
        val body = LinkedMultiValueMap<String, Any>()
        body.add("tilleggsinformasjonJson", createHttpEntity(tilleggsinformasjonJson, "tilleggsinformasjonJson", null, APPLICATION_JSON_VALUE))
        body.add("soknadJson", createHttpEntity(tilleggsinformasjonJson, "soknadJson", null, APPLICATION_JSON_VALUE))
        body.add("vedleggJson", createHttpEntity(tilleggsinformasjonJson, "vedleggJson", null, APPLICATION_JSON_VALUE))
        filer.forEach {
            body.add("metadata", createHttpEntity(getJson(it), "metadata", null, APPLICATION_JSON_VALUE))
            body.add(it.filnavn, createHttpEntity(InputStreamResource(it.data), it.filnavn, it.filnavn, APPLICATION_OCTET_STREAM_VALUE))
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = fiksWebClient.post()
                .uri("$digisosApiEndpoint/digisos/api/v2/soknader/{kommunenummer}/{behandlingsId}", kommunenummer, behandlingsId)
                .header("requestid", UUID.randomUUID().toString())
                .header(AUTHORIZATION, token)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .block() ?: throw SosialhjelpSoknadApiException("Opplasting av $behandlingsId til fiks-digisos-api returnerte null -> kaster feil da vi forventer digisosId eller feilmelding")

            val digisosId = Utils.stripVekkFnutter(response)
            log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
            return digisosId
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            val fiksDigisosId = Utils.getDigisosIdFromResponse(errorResponse, behandlingsId)
            if (fiksDigisosId != null) {
                log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                return fiksDigisosId
            }
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet", e)
        }


//        val entitybuilder = MultipartEntityBuilder.create()
//        entitybuilder.setCharset(StandardCharsets.UTF_8)
//        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//
//        entitybuilder.addTextBody("tilleggsinformasjonJson", tilleggsinformasjonJson, ContentType.APPLICATION_JSON) // Må være første fil
//        entitybuilder.addTextBody("soknadJson", soknadJson, ContentType.APPLICATION_JSON)
//        entitybuilder.addTextBody("vedleggJson", vedleggJson, ContentType.APPLICATION_JSON)
//
//        // hvordan blir dette med mellomlagrede vedlegg? `filer` inneholder kun genererte pdf'er?
//        filer.forEach {
//            entitybuilder.addTextBody("metadata", getJson(it))
//            entitybuilder.addBinaryBody(it.filnavn, it.data, ContentType.APPLICATION_OCTET_STREAM, it.filnavn)
//        }
//
//        try {
//            clientBuilder.build().use { client ->
//                val post = HttpPost("$digisosApiEndpoint/digisos/api/v2/soknader/$kommunenummer/$behandlingsId")
//                post.setHeader("requestid", UUID.randomUUID().toString())
//                post.setHeader(HttpHeader.AUTHORIZATION.name, token)
//                post.setHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
//                post.setHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
//                post.entity = entitybuilder.build()
//
//                val startTime = System.currentTimeMillis()
//                val response = client.execute(post)
//                val endTime = System.currentTimeMillis()
//                if (response.statusLine.statusCode >= 300) {
//                    val errorResponse = EntityUtils.toString(response.entity)
//                    val fiksDigisosId = Utils.getDigisosIdFromResponse(errorResponse, behandlingsId)
//                    if (fiksDigisosId != null) {
//                        log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
//                        return fiksDigisosId
//                    }
//                    throw IllegalStateException(
//                        "Opplasting av $behandlingsId til fiks-digisos-api feilet etter ${endTime - startTime} ms med status ${response.statusLine.reasonPhrase} og response: $errorResponse"
//                    )
//                }
//                val digisosId = Utils.stripVekkFnutter(EntityUtils.toString(response.entity))
//                log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
//                return digisosId
//            }
//        } catch (e: IOException) {
//            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet", e)
//        }
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
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
        private val log by logger()

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
