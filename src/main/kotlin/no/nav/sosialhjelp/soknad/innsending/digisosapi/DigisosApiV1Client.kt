package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.getDigisosIdFromResponse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.stripVekkFnutter
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilForOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.IOException
import java.util.Collections
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
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val fiksWebClient: WebClient
) : DigisosApiV1Client {

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
                    FilForOpplasting(
                        filnavn = dokument.metadata.filnavn,
                        metadata = dokument.metadata,
                        data = krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate)
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
        body.add("soknadJson", createHttpEntity(soknadJson, "soknadJson", null, APPLICATION_JSON_VALUE))
        body.add("vedleggJson", createHttpEntity(vedleggJson, "vedleggJson", null, APPLICATION_JSON_VALUE))

        filer.forEachIndexed { index, fil ->
            body.add("metadata$index", createHttpEntity(getJson(fil), "metadata$index", null, TEXT_PLAIN_VALUE))
            body.add(fil.filnavn, createHttpEntity(ByteArrayResource(IOUtils.toByteArray(fil.data)), fil.filnavn, fil.filnavn, APPLICATION_OCTET_STREAM_VALUE))
        }

        log.info("Send søknad Fiks - POST /digisos/api/v1/soknader/$kommunenummer/$behandlingsId")
        val startTime = System.currentTimeMillis()
        try {
            val response = fiksWebClient.post()
                .uri("$digisosApiEndpoint/digisos/api/v1/soknader/{kommunenummer}/{behandlingsId}", kommunenummer, behandlingsId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .retryWhen(
                    RetryUtils.DEFAULT_RETRY_SERVER_ERRORS.doAfterRetry {
                        log.info("Retry nummer ${it.totalRetries()}")
                    }
                )
                .block() ?: throw FiksException("Fiks - noe uventet feilet ved innsending av søknad. Response er null?", null)

            val digisosId = stripVekkFnutter(response)
            log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
            return digisosId
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            val fiksDigisosId = getDigisosIdFromResponse(errorResponse, behandlingsId)
            if (fiksDigisosId != null) {
                log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                return fiksDigisosId
            }
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet", e)
        }
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val log by logger()
    }
}
