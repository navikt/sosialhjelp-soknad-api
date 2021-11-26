package no.nav.sosialhjelp.soknad.client.fiks.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.ks.kryptering.CMSKrypteringImpl
import no.ks.kryptering.CMSStreamKryptering
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.Utils.getDigisosIdFromResponse
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilMetadata
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.stripVekkFnutter
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.toEntity
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

interface DigisosApiClient {

    fun ping()

    fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String
    ): String
}

class DigisosApiClientImpl(
    private val fiksWebClient: WebClient,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val properties: DigisosApiProperties
) : DigisosApiClient {

    private val executor = ExecutorCompletionService<Void>(Executors.newCachedThreadPool())
    private val kryptering: CMSStreamKryptering = CMSKrypteringImpl()

    override fun ping() {
        val kommuneInfo = kommuneInfoService.hentAlleKommuneInfo()
        check(kommuneInfo != null && kommuneInfo.isNotEmpty()) { "Fikk ikke kontakt med digisosapi" }
    }

    override fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String
    ): String {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(dokumenter.size))
        val digisosId: String
        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate(token)
            digisosId = lastOppFiler(
                soknadJson,
                tilleggsinformasjonJson,
                vedleggJson,
                dokumenter.map {
                    FilOpplasting(
                        it.metadata,
                        krypter(it.data, krypteringFutureList, fiksX509Certificate)
                    )
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

    private fun krypter(
        dokumentStream: InputStream,
        krypteringFutureList: MutableList<Future<Void>>,
        fiksX509Certificate: X509Certificate
    ): InputStream {
        val pipedInputStream = PipedInputStream()
        try {
            val pipedOutputStream = PipedOutputStream(pipedInputStream)
            val krypteringFuture: Future<Void> = executor.submit {
                try {
                    if (ServiceUtils.isNonProduction() && MockUtils.isMockAltProfil()) {
                        IOUtils.copy(dokumentStream, pipedOutputStream)
                    } else {
                        kryptering.krypterData(
                            pipedOutputStream,
                            dokumentStream,
                            fiksX509Certificate,
                            Security.getProvider("BC")
                        )
                    }
                } catch (e: Exception) {
                    log.error("Encryption failed, setting exception on encrypted InputStream", e)
                    throw IllegalStateException("An error occurred during encryption", e)
                } finally {
                    try {
                        log.debug("Closing encryption OutputStream")
                        pipedOutputStream.close()
                        log.debug("Encryption OutputStream closed")
                    } catch (e: IOException) {
                        log.error("Failed closing encryption OutputStream", e)
                    }
                }
                null
            }
            krypteringFutureList.add(krypteringFuture)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return pipedInputStream
    }

    private fun waitForFutures(krypteringFutureList: List<Future<Void>>) {
        for (voidFuture in krypteringFutureList) {
            try {
                voidFuture[300, TimeUnit.SECONDS]
            } catch (e: CompletionException) {
                throw IllegalStateException(e.cause)
            } catch (e: ExecutionException) {
                throw IllegalStateException(e)
            } catch (e: TimeoutException) {
                throw IllegalStateException(e)
            } catch (e: InterruptedException) {
                throw IllegalStateException(e)
            }
        }
    }

    private fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenummer: String,
        navEksternRefId: String,
        token: String
    ): String {
        val multipartData = multipartData(dokumenter, tilleggsinformasjonJson, soknadJson, vedleggJson)
        try {
            val startTime = System.currentTimeMillis()

            val entity: ResponseEntity<String> = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(HttpServerErrorException::class)
                ) {
                    fiksWebClient.post()
                        .uri("/digisos/api/v1/soknader/$kommunenummer/$navEksternRefId")
                        .header("requestid", UUID.randomUUID().toString())
                        .header(HeaderConstants.HEADER_INTEGRASJON_ID, properties.integrasjonsidFiks)
                        .header(HeaderConstants.HEADER_INTEGRASJON_PASSORD, properties.integrasjonpassordFiks)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(multipartData))
                        .retrieve()
                        .toEntity<String>()
                        .doOnError({ it is WebClientResponseException }) {
                            val errorResponse = (it as WebClientResponseException).responseBodyAsString
                            val digisosIdFromResponse = getDigisosIdFromResponse(errorResponse, navEksternRefId)
                            val endTime = System.currentTimeMillis()
                            digisosIdFromResponse
                                ?.let { digisosId ->
                                    log.warn("Søknad $navEksternRefId er allerede sendt til fiks-digisos-api med id $digisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                                    digisosId
                                }
                                ?: throw IllegalStateException("Opplasting av $navEksternRefId til fiks-digisos-api feilet etter ${endTime - startTime} ms med status ${it.statusCode.reasonPhrase} og response: $errorResponse")
                        }
                        .awaitSingle()
                }
            }

            val digisosId = stripVekkFnutter(entity.body)
            log.info("Sendte inn søknad $navEksternRefId til kommune $kommunenummer og fikk digisosid: $digisosId")
            return digisosId
        } catch (e: Exception) {
            throw IllegalStateException("Opplasting av $navEksternRefId til fiks-digisos-api feilet", e)
        }
    }

    private fun multipartData(
        dokumenter: List<FilOpplasting>,
        tilleggsinformasjonJson: String,
        soknadJson: String,
        vedleggJson: String
    ): MultiValueMap<String, HttpEntity<*>> {
        val filerForOpplasting = dokumenter
            .map {
                FilForOpplasting.builder<Any>()
                    .filnavn(it.metadata.filnavn)
                    .metadata(
                        FilMetadata()
                            .withFilnavn(it.metadata.filnavn)
                            .withMimetype(it.metadata.mimetype)
                            .withStorrelse(it.metadata.storrelse)
                    )
                    .data(it.data)
                    .build()
            }

        val multipart = MultipartBodyBuilder()

        multipart.part("tilleggsinformasjonJson", tilleggsinformasjonJson, MediaType.APPLICATION_JSON)
        multipart.part("soknadJson", soknadJson, MediaType.APPLICATION_JSON)
        multipart.part("vedleggJson", vedleggJson, MediaType.APPLICATION_JSON)

        filerForOpplasting.forEach {
            multipart.part("metadata", getJson(it), MediaType.APPLICATION_JSON)
            multipart.part(it.filnavn, it.data, APPLICATION_OCTET_STREAM)
        }

//        val body = LinkedMultiValueMap<String, Any>().apply {
//            add("tilleggsinformasjonJson", createHttpEntityOfString(tilleggsinformasjonJson, "tilleggsinformasjonJson"))
//            add("soknadJson", createHttpEntityOfString(soknadJson, "soknadJson"))
//            add("vedleggJson", createHttpEntityOfString(vedleggJson, "vedleggJson"))
//        }
//
//        filerForOpplasting.forEach {
//            body.add("metadata", createHttpEntity(getJson(it), "metadata", null, "application/json;charset=UTF-8"))
//            body.add(it.filnavn, createHttpEntityOfFile(it, it.filnavn))
//        }

        return multipart.build()
    }

    private fun createHttpEntityOfString(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "application/json;charset=UTF-8")
    }

    private fun createHttpEntityOfFile(file: FilForOpplasting<Any>, name: String): HttpEntity<Any> {
        return createHttpEntity(InputStreamResource(file.data), name, file.filnavn, APPLICATION_OCTET_STREAM_VALUE)
    }

    private fun createHttpEntity(body: Any, name: String, filename: String?, mediaType: String): HttpEntity<Any> {
        val headerMap = LinkedMultiValueMap<String, String>()
        val builder = ContentDisposition
            .builder("form-data")
            .name(name)
        filename?.let { builder.filename(it) }
        val contentDisposition = builder.build()

        headerMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        headerMap.add(HttpHeaders.CONTENT_TYPE, mediaType)
        return HttpEntity(body, headerMap)
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigisosApiClientImpl::class.java)
    }
}
