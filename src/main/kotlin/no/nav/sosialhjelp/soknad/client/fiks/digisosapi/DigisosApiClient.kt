package no.nav.sosialhjelp.soknad.client.fiks.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.ks.kryptering.CMSKrypteringImpl
import no.ks.kryptering.CMSStreamKryptering
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.Utils.getDigisosIdFromResponse
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilMetadata
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.stripVekkFnutter
import org.apache.commons.io.IOUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.eclipse.jetty.http.HttpHeader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.charset.StandardCharsets
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
        val multipartEntity = multipartEntity(dokumenter, tilleggsinformasjonJson, soknadJson, vedleggJson)

        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
            .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
            .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
            .build()

        try {
            HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()
                .use { client ->
                    val post = HttpPost(properties.digisosApiEndpoint + "/digisos/api/v1/soknader/$kommunenummer/$navEksternRefId")
                    post.setHeader("requestid", UUID.randomUUID().toString())
                    post.setHeader(HttpHeader.AUTHORIZATION.name, token)
                    post.setHeader(HEADER_INTEGRASJON_ID, properties.integrasjonsidFiks)
                    post.setHeader(HEADER_INTEGRASJON_PASSORD, properties.integrasjonpassordFiks)
                    post.entity = multipartEntity
                    val startTime = System.currentTimeMillis()
                    val response: CloseableHttpResponse = client.execute(post)
                    val endTime = System.currentTimeMillis()
                    if (response.statusLine.statusCode >= 300) {
                        val errorResponse = EntityUtils.toString(response.entity)
                        val fiksDigisosId = getDigisosIdFromResponse(errorResponse, navEksternRefId)
                        if (fiksDigisosId != null) {
                            log.warn("Søknad $navEksternRefId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                            return fiksDigisosId
                        }
                        throw IllegalStateException("Opplasting av $navEksternRefId til fiks-digisos-api feilet etter ${endTime - startTime} ms med status ${response.statusLine.reasonPhrase} og response: $errorResponse")
                    }
                    val digisosId = stripVekkFnutter(EntityUtils.toString(response.entity))
                    log.info(
                        "Sendte inn søknad {} til kommune {} og fikk digisosid: {}",
                        navEksternRefId,
                        kommunenummer,
                        digisosId
                    )
                    return digisosId
                }
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $navEksternRefId til fiks-digisos-api feilet", e)
        }
    }

    private fun multipartEntity(
        dokumenter: List<FilOpplasting>,
        tilleggsinformasjonJson: String,
        soknadJson: String,
        vedleggJson: String
    ): org.apache.http.HttpEntity? {
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

        val entitybuilder = MultipartEntityBuilder.create()
        entitybuilder.setCharset(StandardCharsets.UTF_8)
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)

        entitybuilder.addTextBody("tilleggsinformasjonJson", tilleggsinformasjonJson, ContentType.APPLICATION_JSON)
        entitybuilder.addTextBody("soknadJson", soknadJson, ContentType.APPLICATION_JSON)
        entitybuilder.addTextBody("vedleggJson", vedleggJson, ContentType.APPLICATION_JSON)

        filerForOpplasting.forEach {
            entitybuilder.addTextBody("metadata", getJson(it))
            entitybuilder.addBinaryBody(it.filnavn, it.data, ContentType.APPLICATION_OCTET_STREAM, it.filnavn)
        }

        return entitybuilder.build()
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

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
