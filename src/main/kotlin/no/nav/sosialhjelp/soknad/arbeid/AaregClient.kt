package no.nav.sosialhjelp.soknad.arbeid

import kotlinx.coroutines.reactor.awaitSingleOrNull
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider.TOKENX
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class AaregClient(
    @param:Value("\${aareg_url}") private val aaregUrl: String,
    @param:Value("\${aareg_audience}") private val aaregAudience: String,
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) {
    suspend fun finnArbeidsforholdForArbeidstaker(): List<ArbeidsforholdDto>? {
        val request = ArbeidsforholdSokRequest(arbeidstakerId = currentUserContext().userId)

        return doFinnArbeidsforhold(request)
    }

    private suspend fun doFinnArbeidsforhold(
        request: ArbeidsforholdSokRequest,
    ): List<ArbeidsforholdDto>? {
        return webClient.post()
            .uri("/v2/arbeidstaker/arbeidsforhold")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getTokenX(currentUserContext().userToken)}")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono<List<ArbeidsforholdDto>>()
            .awaitSingleOrNull()
    }

    private suspend fun getTokenX(userToken: String) = texasService.exchangeToken(userToken, TOKENX, target = aaregAudience)

    private val webClient: WebClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient())
            .baseUrl(aaregUrl)
            .codecs { it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper)) }
            .build()
}

private data class ArbeidsforholdSokRequest(
    val arbeidstakerId: String,
    val arbeidsstedId: String? = null,
    val opplysningspliktigId: String? = null,
    // bruk default i api
    val arbeidsforholdtyper: List<String>? = null,
    // bruk default i api
    val rapporteringsordninger: List<String>? = null,
    // bruk default i api
    val arbeidsforholdstatuser: List<String>? = null,
    val historikk: Boolean = false,
    val sporingsinformasjon: Boolean = false,
)
