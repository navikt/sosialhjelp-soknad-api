package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.MdcExchangeFilter
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.fiksServiceConnectionProvider
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.sosialhjelpJsonMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilForOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
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
import java.util.UUID
import java.util.concurrent.Future

@Component
class DigisosApiV2Client(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val fiksHttpClient =
        HttpClient.create(fiksServiceConnectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, THIRTY_SECONDS.toMillis().toInt())
            .doOnConnected {
                it
                    .addHandlerLast(ReadTimeoutHandler(THIRTY_SECONDS.toSeconds().toInt()))
                    .addHandlerLast(WriteTimeoutHandler(THIRTY_SECONDS.toSeconds().toInt()))
            }
            .responseTimeout(TWO_MINUTES)

    private val fiksWebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(fiksHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(sosialhjelpJsonMapper))
                it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(sosialhjelpJsonMapper))
            }
            .defaultHeader(Constants.HEADER_INTEGRASJON_ID, integrasjonsidFiks)
            .defaultHeader(Constants.HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
            .filter(MdcExchangeFilter)
            .build()

    fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        pdfDokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: UUID,
    ): UUID {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(pdfDokumenter.size))
        val digisosId: UUID
        try {
            digisosId =
                lastOppFiler(
                    soknadJson,
                    tilleggsinformasjonJson,
                    vedleggJson,
                    pdfDokumenter.map { dokument: FilOpplasting ->
                        FilForOpplasting(
                            filnavn = dokument.metadata.filnavn,
                            metadata = dokument.metadata,
                            data = krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate),
                        )
                    },
                    kommunenr,
                    navEksternRefId,
                )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        return digisosId
    }

    fun getSoknader(): List<DigisosSak> {
        val startTime = System.currentTimeMillis()
        return try {
            fiksWebClient
                .get()
                .uri("$digisosApiEndpoint/digisos/api/v1/soknader/soknader")
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $userToken")
                .retrieve()
                .bodyToMono<List<DigisosSak>>()
                .block()
                ?: throw FiksException(
                    message = "Fiks - noe uventet feilet ved henting av søknader. Response er null?",
                    cause = null,
                )
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            throw IllegalStateException("Henting av søknader hos Fiks feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Henting av søknader hos Fiks feilet", e)
        }
    }

    fun getInnsynsfil(
        digisosId: String,
        dokumentLagerId: String,
    ): JsonDigisosSoker {
        val startTime = System.currentTimeMillis()
        return try {
            fiksWebClient
                .get()
                .uri(
                    "$digisosApiEndpoint/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}",
                    digisosId,
                    dokumentLagerId,
                )
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $userToken")
                .retrieve()
                .bodyToMono<JsonDigisosSoker>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?: throw FiksException("Fiks - noe uventet feilet ved henting av innsynsfil. Response er null?", null)
        } catch (e: WebClientResponseException) {
            val errorResponse = sosialhjelpJsonMapper.readValue(e.responseBodyAsString, ErrorMessage::class.java)
            throw IllegalStateException("Henting av innsynsfil hos Fiks feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Henting av innsynsfil hos Fiks feilet", e)
        }
    }

    fun getStatusForSoknader(
        digisosIdListe: List<UUID>,
    ): FiksSoknadStatusListe {
        val startTime = System.currentTimeMillis()

        val sporingsId = UUID.randomUUID().toString()
        val fiksSoknaderStatusRequest = FiksSoknaderStatusRequest(digisosIdListe)

        return try {
            fiksWebClient
                .post()
                .uri("$digisosApiEndpoint/digisos/api/v1/nav/soknader/status".plus("?sporingsId=$sporingsId"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "BEARER $maskinportenToken")
                .bodyValue(fiksSoknaderStatusRequest)
                .retrieve()
                .bodyToMono<FiksSoknadStatusListe>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?: throw FiksException(
                    message = "Fiks - noe uventet feilet ved henting av status for søknader. Response er null?",
                    cause = null,
                )
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            throw IllegalStateException(
                "Henting av status for søknader hos Fiks feilet etter " +
                    "${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} " +
                    "og response: $errorResponse. SporingsId: $sporingsId",
            )
        } catch (e: IOException) {
            throw IllegalStateException("Henting av status for søknader hos Fiks feilet. SporingsId: $sporingsId", e)
        }
    }

    private fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilForOpplasting<Any>>,
        kommunenummer: String,
        soknadId: UUID,
    ): UUID {
        val body = LinkedMultiValueMap<String, Any>()
        body.add(
            "tilleggsinformasjonJson",
            createHttpEntity(tilleggsinformasjonJson, "tilleggsinformasjonJson", null, APPLICATION_JSON_VALUE),
        )
        body.add("soknadJson", createHttpEntity(soknadJson, "soknadJson", null, APPLICATION_JSON_VALUE))
        body.add("vedleggJson", createHttpEntity(vedleggJson, "vedleggJson", null, APPLICATION_JSON_VALUE))

        filer.forEachIndexed { index, fil ->
            body.add("metadata$index", createHttpEntity(getJson(fil), "metadata$index", null, TEXT_PLAIN_VALUE))
            body.add(
                fil.filnavn,
                createHttpEntity(
                    ByteArrayResource(IOUtils.toByteArray(fil.data)),
                    fil.filnavn,
                    fil.filnavn,
                    APPLICATION_OCTET_STREAM_VALUE,
                ),
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val response =
                fiksWebClient
                    .post()
                    .uri(
                        "$digisosApiEndpoint/digisos/api/v2/soknader/{kommunenummer}/{behandlingsId}",
                        kommunenummer,
                        soknadId,
                    )
                    .header(AUTHORIZATION, "Bearer $userToken")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono<String>()
                    .block()
                    ?: throw FiksException(
                        message = "Fiks - noe uventet feilet ved innsending av søknad. Response er null?",
                        cause = null,
                    )

            val digisosId = Utils.stripVekkFnutter(response).let { UUID.fromString(it) }
            log.info("Sendte inn søknad til kommune $kommunenummer og fikk digisosid: $digisosId")

            return digisosId
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            val digisosId = Utils.getDigisosIdFromResponse(errorResponse, soknadId)

            when {
                digisosId != null -> handleAlleredeMottatt(digisosId, soknadId, errorResponse)
                else -> throw IllegalStateException(
                    "Opplasting av $soknadId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - startTime} " +
                        "ms med status ${e.statusCode} og response: $errorResponse",
                )
            }
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $soknadId til fiks-digisos-api feilet", e)
        }
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String =
        try {
            sosialhjelpJsonMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }

    private fun handleAlleredeMottatt(
        digisosId: UUID,
        soknadId: UUID,
        errorResponse: String,
    ): Nothing {
        log.warn(
            "Søknad $soknadId er allerede sendt med id $digisosId. " +
                "Returner exception med digisos-id så brukeren blir rutet til innsyn. " +
                "ErrorResponse var: $errorResponse",
        )
        throw AlleredeMottattException(
            digisosId = digisosId,
            message = "Søknad $soknadId er allerede sendt med id $digisosId. ErrorResponse var: $errorResponse",
        )
    }

    private val fiksX509Certificate get() = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
    private val maskinportenToken get() = texasService.getToken(IdentityProvider.M2M, "ks:fiks")
    private val userToken get() = SubjectHandlerUtils.getTokenOrNull() ?: error("Mangler userToken")

    companion object {
        private val log by logger()
        private val THIRTY_SECONDS = Duration.ofSeconds(30)
        private val TWO_MINUTES = Duration.ofMinutes(2)
    }
}

data class FiksSoknaderStatusRequest(
    val digisosIdListe: List<UUID>,
)

data class FiksSoknadStatusListe(
    val statusListe: List<FiksSoknadStatus>,
)

data class FiksSoknadStatus(
    val digisosId: UUID,
    val levertFagsystem: Boolean,
)

class AlleredeMottattException(
    val digisosId: UUID,
    message: String,
) : SosialhjelpSoknadApiException(message)
