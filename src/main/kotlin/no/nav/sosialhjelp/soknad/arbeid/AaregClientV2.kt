package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDtoV2
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider.TOKENX
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class AaregClientV2(
    @param:Value("\${aareg_url}") private val aaregUrl: String,
    @param:Value("\${aareg_audience}") private val aaregAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    fun finnArbeidsforholdForArbeidstaker(): List<ArbeidsforholdDtoV2>? {
        val request = ArbeidsforholdSokRequest(arbeidstakerId = getUserIdFromToken())

        val arbeidsforholdResponse = doFinnArbeidsforhold(request)
        logger.info("Hentet arbeidsforhold: $arbeidsforholdResponse")

        return arbeidsforholdResponse?.let { jacksonObjectMapper().readValue(arbeidsforholdResponse) }
    }

//    fun finnArbeidsforholdForArbeidstaker(): List<ArbeidsforholdDtoV2>? {
//        val request = ArbeidsforholdSokRequest(arbeidstakerId = getUserIdFromToken())
//
//        return doFinnArbeidsforhold(request)
//    }

    private fun doFinnArbeidsforhold(request: ArbeidsforholdSokRequest): String? {
        return webClient.post()
            .uri("/v2/arbeidstaker/arbeidsforhold")
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono<String>()
            .block()
    }

//    private fun doFinnArbeidsforhold(request: ArbeidsforholdSokRequest): List<ArbeidsforholdDtoV2>? {
//        return webClient.post()
//            .uri("/v2/arbeidstaker/arbeidsforhold")
//            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
//            .body(BodyInserters.fromValue(request))
//            .retrieve()
//            .bodyToMono<List<ArbeidsforholdDtoV2>>()
//            .block()
//    }

    private fun getAuthHeader() = "Bearer $tokenXToken"

    private val tokenXToken: String get() = texasService.exchangeToken(TOKENX, target = aaregAudience)

    private val webClient: WebClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient())
            .baseUrl(aaregUrl)
            .codecs { it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper)) }
            .build()

    companion object {
        private val logger by logger()
    }
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

private data class Sokeperiode(
    private val fomDate: LocalDate,
    private val tomDate: LocalDate,
) {
    val fom: String get() = fomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val tom: String get() = tomDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
