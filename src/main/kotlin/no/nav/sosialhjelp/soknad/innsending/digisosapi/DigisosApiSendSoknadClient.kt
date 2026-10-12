package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID

@Component
class DigisosApiSendSoknadClient(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    webClientBuilder: WebClient.Builder,
) {
    private val fiksWebClient = FiksUtils.createFiksWebClient(webClientBuilder, integrasjonsidFiks, integrasjonpassordFiks)
    private val userToken get() = SubjectHandlerUtils.getTokenOrNull() ?: error("Mangler userToken")

    fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilOpplasting>,
        kommunenummer: String,
        soknadId: UUID,
    ): SendSoknadResponse {
        val body = FiksUtils.createBody(soknadJson, tilleggsinformasjonJson, vedleggJson, filer)

        val startTime = System.currentTimeMillis()

        return runCatching {
            doLastOppFiler(
                soknadId = soknadId,
                kommunenummer = kommunenummer,
                body = body,
            )
                .let { SendSoknadResponse.Success(it) }
        }
            .getOrElse { e ->
                when (val errorMessage = e.toFiksErrorMessageOrNull()) {
                    null -> SendSoknadResponse.Error(e, startTime)
                    else -> SendSoknadResponse.FiksError(errorMessage, e, startTime)
                }
            }
    }

    private fun doLastOppFiler(
        soknadId: UUID,
        kommunenummer: String,
        body: LinkedMultiValueMap<String, Any>,
    ): UUID {
        return fiksWebClient
            .post()
            .uri("$digisosApiEndpoint/digisos/api/v2/soknader/{kommunenummer}/{soknadId}", kommunenummer, soknadId)
            .header(AUTHORIZATION, "Bearer $userToken")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono<String>()
            .block()
            // digisosId
            ?.let { UUID.fromString(FiksUtils.stripVekkFnutter(it)) }
            ?: error("Fiks - noe uventet feilet ved innsending av søknad. Response er null?")
    }
}

private fun Throwable.toFiksErrorMessageOrNull(): ErrorMessage? =
    runCatching {
        when (this) {
            is WebClientResponseException -> jacksonObjectMapper().readValue(responseBodyAsString, ErrorMessage::class.java)
            else -> null
        }
    }.getOrNull()


sealed interface SendSoknadResponse {
    class Success(val digisosId: UUID) : SendSoknadResponse
    class FiksError(val errorMessage: ErrorMessage, val e: Throwable, val startTime: Long) : SendSoknadResponse
    class Error(val e: Throwable, val startTime: Long) : SendSoknadResponse
}
