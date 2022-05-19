package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
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
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val webClient: WebClient
) : DigisosApiV2Client {

//    private val retryHandler = DefaultHttpRequestRetryHandler()
//    private val serviceUnavailableRetryStrategy = FiksServiceUnavailableRetryStrategy()
//
//    private val requestConfig = RequestConfig.custom()
//        .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
//        .build()
//
//    private val clientBuilder = HttpClientBuilder.create()
//        .setRetryHandler(retryHandler)
//        .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
//        .setDefaultRequestConfig(requestConfig)
//        .useSystemProperties()

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
        body.add("tilleggsinformasjon.json", createHttpEntityOfString(tilleggsinformasjonJson, "tilleggsinformasjon.json"))
        body.add("soknad.json", createHttpEntityOfString(soknadJson, "soknad.json"))
        body.add("vedlegg.json", createHttpEntityOfString(vedleggJson, "vedlegg.json"))
        filer.forEach {
            body.add("metadata", createHttpEntityOfString(getJson(it), "metadata"))
            body.add(it.filnavn, createHttpEntityOfFile(it, it.filnavn))
        }

        try {
            val startTime = System.currentTimeMillis()
            val mottattDigisosId = webClient.post()
                .uri(INNSENDING_V2_PATH, kommunenummer, behandlingsId)
                .header(AUTHORIZATION, token)
                .header("requestid", UUID.randomUUID().toString())
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .doOnError(WebClientResponseException::class.java) {
                    log.warn("Innsending av søknad $behandlingsId feilet etter ${System.currentTimeMillis() - startTime} ms med status ${it.statusCode} og response: ${it.responseBodyAsString}")
                }
                .block() ?: throw RuntimeException("digisosId er null")
            val digisosId = Utils.stripVekkFnutter(mottattDigisosId)
            log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
            return digisosId
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            val fiksDigisosId = Utils.getDigisosIdFromResponse(errorResponse, behandlingsId)
            if (fiksDigisosId != null) {
                log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                return fiksDigisosId
            }
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet med response: $errorResponse")
        }
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
//                post.setHeader(Constants.HEADER_INTEGRASJON_ID, integrasjonsidFiks)
//                post.setHeader(Constants.HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
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
//    }

    private fun createHttpEntityOfString(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "text/plain;charset=UTF-8")
    }

    private fun createHttpEntityOfJson(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "application/json;charset=UTF-8")
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

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            Utils.digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val log by logger()

        private const val INNSENDING_V2_PATH = "digisos/api/v2/soknader/{kommunenummer}/{behandlingsId}"
//        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
