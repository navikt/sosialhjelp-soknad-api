package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider.TOKENX
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.v2.register.UserContext
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
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    fun finnArbeidsforholdForArbeidstaker(userContext: UserContext): List<ArbeidsforholdDto>? {
        val request = ArbeidsforholdSokRequest(arbeidstakerId = userContext.userId)

        return doFinnArbeidsforhold(userContext.token, request)
    }

    private fun doFinnArbeidsforhold(
        token: String,
        request: ArbeidsforholdSokRequest,
    ): List<ArbeidsforholdDto>? {
        return webClient.post()
            .uri("/v2/arbeidstaker/arbeidsforhold")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getTokenX(token)}")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono<List<ArbeidsforholdDto>>()
            .block()
    }

    private fun getTokenX(personId: String) = texasService.exchangeToken(personId, TOKENX, target = aaregAudience)

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
