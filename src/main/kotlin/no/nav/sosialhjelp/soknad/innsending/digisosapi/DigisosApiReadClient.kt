package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksUtils.sosialhjelpJsonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.IOException
import java.util.UUID

@Component
class DigisosApiReadClient(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val fiksWebClient = FiksUtils.createFiksWebClient(webClientBuilder, integrasjonsidFiks, integrasjonpassordFiks)

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

    private val maskinportenToken get() = texasService.getToken(IdentityProvider.M2M, "ks:fiks")
    private val userToken get() = SubjectHandlerUtils.getTokenOrNull() ?: error("Mangler userToken")
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
