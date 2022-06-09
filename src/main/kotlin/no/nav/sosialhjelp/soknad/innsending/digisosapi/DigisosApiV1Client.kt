package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.getDigisosIdFromResponse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.stripVekkFnutter
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.eclipse.jetty.http.HttpHeader.AUTHORIZATION
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future

interface DigisosApiV1Client {

    fun ping()

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

class DigisosApiV1ClientImpl(
    private val digisosApiEndpoint: String,
    private val integrasjonsidFiks: String,
    private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService
) : DigisosApiV1Client {

    private val retryHandler = DefaultHttpRequestRetryHandler()
    private val serviceUnavailableRetryStrategy = FiksServiceUnavailableRetryStrategy()

    private val requestConfig = RequestConfig.custom()
        .setConnectTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .setConnectionRequestTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .setSocketTimeout(SENDING_TIL_FIKS_TIMEOUT)
        .build()

    override fun ping() {
        val kommuneInfo = kommuneInfoService.hentKommuneInfoFraFiks()
        check(kommuneInfo.isNotEmpty()) { "Fikk ikke kontakt med digisosapi" }
    }

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
                        .metadata(dokument.metadata)
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

    private fun clientBuilder(): HttpClientBuilder {
        return HttpClientBuilder.create()
            .setRetryHandler(retryHandler)
            .setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy)
            .useSystemProperties()
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
        val entitybuilder = MultipartEntityBuilder.create()
        entitybuilder.setCharset(StandardCharsets.UTF_8)
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)

        entitybuilder.addTextBody("tilleggsinformasjonJson", tilleggsinformasjonJson, APPLICATION_JSON) // Må være første fil
        entitybuilder.addTextBody("soknadJson", soknadJson, APPLICATION_JSON)
        entitybuilder.addTextBody("vedleggJson", vedleggJson, APPLICATION_JSON)
        filer.forEachIndexed {index, fil ->
            entitybuilder.addTextBody("metadata:${index}", getJson(fil))
            entitybuilder.addBinaryBody(fil.filnavn, fil.data, APPLICATION_OCTET_STREAM, fil.filnavn)
        }

        try {
            clientBuilder().setDefaultRequestConfig(requestConfig).build().use { client ->
                val post = HttpPost("$digisosApiEndpoint/digisos/api/v1/soknader/$kommunenummer/$behandlingsId")
                post.setHeader("requestid", UUID.randomUUID().toString())
                post.setHeader(AUTHORIZATION.name, token)
                post.setHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
                post.setHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
                post.entity = entitybuilder.build()

                val startTime = System.currentTimeMillis()
                val response = client.execute(post)
                val endTime = System.currentTimeMillis()
                if (response.statusLine.statusCode >= 300) {
                    val errorResponse = EntityUtils.toString(response.entity)
                    val fiksDigisosId = getDigisosIdFromResponse(errorResponse, behandlingsId)
                    if (fiksDigisosId != null) {
                        log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                        return fiksDigisosId
                    }
                    throw IllegalStateException(
                        "Opplasting av $behandlingsId til fiks-digisos-api feilet etter ${endTime - startTime} ms med status ${response.statusLine.reasonPhrase} og response: $errorResponse"
                    )
                }
                val digisosId = stripVekkFnutter(EntityUtils.toString(response.entity))
                log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
                return digisosId
            }
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet", e)
        }
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String? {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val log by logger()

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
