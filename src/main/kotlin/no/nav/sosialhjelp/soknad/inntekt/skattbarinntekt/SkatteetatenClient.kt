package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.maskerFnr
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntekt
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Sokedata
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class SkatteetatenClient(
    @param:Value("\${skatteetaten_api_baseurl}") private val baseurl: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val skatteetatenWebClient: WebClient =
        webClientBuilder.configureWebClientBuilder(createDefaultHttpClient())
            .baseUrl(baseurl)
            .build()

    fun hentSkattbarinntekt(): SkattbarInntektResponse =
        runCatching { doHentSkattbarInntekt(createSokedata()) }.getOrElse { e -> handleError(e) }

    private fun doHentSkattbarInntekt(sokedata: Sokedata): SkattbarInntektResponse {
        return skatteetatenWebClient.get()
            .uri(
                "{personidentifikator}/inntekter?fraOgMed={fom}&tilOgMed={tom}",
                sokedata.identifikator,
                sokedata.fom.format(formatter),
                sokedata.tom.format(formatter),
            )
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getMaskinportenToken())
            .retrieve()
            .bodyToMono<SkattbarInntekt>()
            .block()
            ?.let { SkattbarInntektResponse.Success(it) }
            ?: error("Respons fra Skatteetaten er null?")
    }

    private fun handleError(e: Throwable): SkattbarInntektResponse =
        when(e) {
            is WebClientResponseException.NotFound -> SkattbarInntektResponse.NotFound
            is WebClientResponseException -> {
                val msg = "Klarer ikke hente skatteopplysninger ${maskerFnr(e.responseBodyAsString)} status ${e.statusCode}"
                SkattbarInntektResponse.Error(msg, e)
            }
            else -> SkattbarInntektResponse.Error("Klarer ikke hente skatteopplysninger", e)
        }

    private fun createSokedata(): Sokedata {
        return Sokedata(
            identifikator = resolveIdent(),
            fom = LocalDate.now().minusMonths(if (LocalDate.now().dayOfMonth > 10) 1 else 2.toLong()),
            tom = LocalDate.now(),
        )
    }

    private fun resolveIdent(): String {
        return SubjectHandlerUtils.getUserIdFromToken()
            .let { if (MiljoUtils.isProduction()) it else System.getenv("TESTBRUKER_SKATT") ?: it }
    }

    private fun getMaskinportenToken(): String =
        texasService.getToken(IdentityProvider.M2M, "skatteetaten:inntekt")

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}

sealed interface SkattbarInntektResponse {
    class Success(val inntekt: SkattbarInntekt) : SkattbarInntektResponse
    class Error(val error: String, val cause: Throwable) : SkattbarInntektResponse
    object NotFound : SkattbarInntektResponse
}
