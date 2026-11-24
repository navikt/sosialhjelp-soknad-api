package no.nav.sosialhjelp.soknad.innsending.digisosapi

import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import no.nav.sosialhjelp.soknad.app.subjecthandler.TokenUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class FiksReadClient(
    @Value("\${digisos_api_baseurl}") digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") integrasjonpassordFiks: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val fiksWebClient = FiksUtils.createFiksWebClient(
        webClientBuilder.baseUrl(digisosApiEndpoint+BASE_PATH),
        integrasjonsidFiks,
        integrasjonpassordFiks
    )

    fun getSoknader(): List<DigisosSak> {
        return runCatching { doGet<List<DigisosSak>>(PATH_SOKNADER, userToken) }
            .getOrElse { e ->
                when (e) {
                    is WebClientResponseException -> {
                        val errorResponse = e.responseBodyAsString
                        throw FiksException("Henting av søknader hos Fiks feilet med status ${e.statusCode} og response: $errorResponse", e)
                    }
                    else -> throw FiksException("Henting av søknader hos Fiks feilet", e)
                }
            }
    }

    fun getDigisosSak(
        digisosId: UUID,
    ): DigisosSak {
        log.debug("Henter søknad fra Fiks. digisosId={}", digisosId)

        return runCatching { doGet<DigisosSak>(PATH_DIGISOSSAK, userToken, digisosId.toString()) }
            .getOrElse { e ->
                if (e is WebClientResponseException) {
                    when (e.statusCode) {
                        HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN -> throw IkkeFunnetException("Fiks ga ${e.statusCode.value()}: ${e.responseBodyAsString}")
                        else ->
                            throw FiksException(
                                "Henting av søknad $digisosId hos Fiks feilet med status ${e.statusCode.value()}"
                                ,e
                            )
                    }
                }
                throw FiksException("Uventet feil ved henting av søknad $digisosId hos Fiks", e)
            }.also { log.debug("Hentet søknad fra Fiks. digisosId={}", digisosId) }
    }

    fun getDigisosSakMedSporingsId(
        digisosId: UUID,
    ): DigisosSak {
        val sporingsId = UUID.randomUUID().toString()
        return runCatching { doGet<DigisosSak>(PATH_DIGISOSSAK.plus("?sporingsId=$sporingsId"), maskinportenToken, digisosId.toString()) }
            .getOrElse { e ->
                if (e is WebClientResponseException) {
                    when (e.statusCode) {
                        HttpStatus.NOT_FOUND -> throw IkkeFunnetException("DigisosSak Not Found: ${e.responseBodyAsString}", e)
                        else -> throw FiksException("Henting av DigisosSak $digisosId hos Fiks feilet: ${e.responseBodyAsString}",e)
                    }
                }
                throw FiksException("Uventet feil ved henting av søknad $digisosId hos Fiks", e)
            }
    }

    fun getInnsynsfil(
        digisosId: String,
        dokumentLagerId: String,
    ): JsonDigisosSoker =
        runCatching { doGet<JsonDigisosSoker>(PATH_INNSYNSFIL, userToken, digisosId, dokumentLagerId) }
            .getOrElse { e ->
                if (e is WebClientResponseException) {
                    throw FiksException("Henting av innsynsfil hos Fiks feilet med status ${e.statusCode} og response: ${e.responseBodyAsString}", e)
                }
                throw FiksException("Henting av innsynsfil hos Fiks feilet", e)
            }

    fun getStatusForSoknader(
        digisosIdListe: List<UUID>,
    ): FiksSoknadStatusListe {
        val sporingsId = UUID.randomUUID().toString()

        return runCatching { doPost<FiksSoknadStatusListe>(FiksSoknaderStatusRequest(digisosIdListe), PATH_STATUS_SOKNADER.plus("?sporingsId=$sporingsId")) }
            .getOrElse { e ->
                when (e) {
                    is WebClientResponseException ->
                        throw FiksException("Henting av status for søknader hos Fiks feilet med status ${e.statusCode} og response: ${e.responseBodyAsString}. SporingsId: $sporingsId", e)
                    else -> throw FiksException("Henting av status for søknader hos Fiks feilet. SporingsId: $sporingsId", e)
                }
            }
    }

    fun getInnsendingsstatus(
        soknadIds: List<UUID>,
    ): List<InnsendingStatusDto> {
        val sporingsId = UUID.randomUUID().toString()

        return runCatching { doPost<List<InnsendingStatusDto>>(InnsendingsstatusRequest(soknadIds), PATH_STATUS_INNSENDING.plus("?sporingsId=$sporingsId")) }
            .getOrElse { ex ->
                when (ex) {
                    is WebClientResponseException ->
                        throw FiksException("Feil ved henting av innsendingsstatus: ${ex.responseBodyAsString}. SporingsId: $sporingsId", ex)
                    else -> throw FiksException("Uventet feil ved henting av innsendingsstatus. SporingsId: $sporingsId", ex)
                }
            }
    }

    private inline fun <reified T: Any> doGet(uri: String, token: String, vararg variables: String?): T =
        fiksWebClient.get()
            .uri(uri, *variables)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, "Bearer $token")
            .retrieve()
            .bodyToMono<T>()
            .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
            .block()
            ?: throw FiksException("Fiks - noe uventet feilet ved henting av data fra $uri. Response er null?", null)

    private inline fun <reified T: Any> doPost(body: Any, uri: String): T =
        fiksWebClient.post()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, "BEARER $maskinportenToken")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<T>()
            .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
            .block()
            ?: throw FiksException("Fiks - noe uventet feilet ved henting av innsendingstatus for søknader. Response er null?", null)

    companion object {
        private val log by logger()
        private const val BASE_PATH = "/digisos/api/v1"
        private const val PATH_SOKNADER = "/soknader/soknader"
        private const val PATH_DIGISOSSAK = "/soknader/{digisosId}"
        private const val PATH_INNSYNSFIL = "/soknader/{digisosId}/dokumenter/{dokumentlagerId}"
        private const val PATH_STATUS_SOKNADER = "/nav/soknader/status"
        private const val PATH_STATUS_INNSENDING = "/nav/soknader/innsendingsstatus"
    }

    private val maskinportenToken get() = texasService.getToken(IdentityProvider.M2M, "ks:fiks")
    private val userToken get() = TokenUtils.getToken()
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

data class InnsendingsstatusRequest(
    val navEksternRefIdListe: List<UUID>,
)

data class InnsendingStatusDto(
    val navEksternRefId: UUID,
    val digisosId: UUID,
    val kommunenummer: String,
    val opprettet: Long,
)
