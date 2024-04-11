package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.mdcExchangeFilter
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilForOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.v2.soknad.OldIdFormatSupportHandler
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.io.IOException
import java.time.Duration
import java.util.Collections
import java.util.concurrent.Future

@Component
class DigisosApiV2Client(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val oldIdFormatSupportHandler: OldIdFormatSupportHandler,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

    private val fiksWebClient = webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .doOnConnected {
                        it
                            .addHandlerLast(ReadTimeoutHandler(SENDING_TIL_FIKS_TIMEOUT / 1000))
                            .addHandlerLast(WriteTimeoutHandler(SENDING_TIL_FIKS_TIMEOUT / 1000))
                    }
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(digisosObjectMapper))
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(digisosObjectMapper))
        }
        .defaultHeader(Constants.HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(Constants.HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .filter(mdcExchangeFilter)
        .build()

    companion object {
        private val log by logger()
        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }

    fun krypterOgLastOppFiler(
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

        val startTime = System.currentTimeMillis()
        try {
            // TODO MIDLERTIDIG - legger ved ID på gammelt format som behandlingsId inntil Oslo kan håndtere
            val idOldFormat: String = oldIdFormatSupportHandler.findByUUID(behandlingsId)?.idOldFormat
                ?: oldIdFormatSupportHandler.createAndMap(behandlingsId).idOldFormat

            val response = fiksWebClient.post()
                .uri("$digisosApiEndpoint/digisos/api/v2/soknader/{kommunenummer}/{behandlingsId}", kommunenummer, idOldFormat)
                .header(AUTHORIZATION, token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block() ?: throw FiksException("Fiks - noe uventet feilet ved innsending av søknad. Response er null?", null)

            val digisosId = Utils.stripVekkFnutter(response)
            log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")

            // TODO MIDLERTIDIG - Knytter ID til ID på gammelt format - se over
            idOldFormat?.let {
                log.warn("MIDLERTIDIG ID-FIKS: Soknad med id $behandlingsId ble sendt med: $idOldFormat")
            }

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
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }
}
