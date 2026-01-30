package no.nav.sosialhjelp.soknad.kodeverk

import io.github.resilience4j.retry.annotation.Retry
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.Serializable
import java.time.LocalDate

@Component
class KodeverkClient(
    @param:Value("\${kodeverk_url}") private val kodeverkUrl: String,
    @param:Value("\${kodeverk_scope}") private val scope: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    fun hentKodeverk(kodeverksnavn: String): KodeverkDto =
        doHentKodeverk(
            kodeverksnavn,
            token = texasService.getToken(IdentityProvider.AZURE_AD, scope),
        )

    @Retry(name = "kodeverk")
    private fun doHentKodeverk(
        kodeverksnavn: String,
        token: String,
    ): KodeverkDto =
        runCatching {
            logger.info("Henter Kodeverk fra Kodeverk-Api")

            webClient
                .get()
                .uri { builder ->
                    builder
                        .path("/api/v1/kodeverk/{kodeverksnavn}/koder/betydninger")
                        .queryParam("spraak", SPRAK_NORSK_BOKMAL)
                        .build(kodeverksnavn)
                }
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .bodyToMono<KodeverkDto>()
                .block() ?: error("Kodeverk - ugyldig data")
        }
            .onFailure { e ->
                when (e) {
                    is WebClientResponseException -> logger.warn("Kodeverk - ${e.statusCode}", e)
                    else -> logger.error("Kodeverk - noe uventet feilet", e)
                }
            }
            .getOrThrow()

    private val webClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient())
            .codecs {
                it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper))
            }
            .baseUrl(kodeverkUrl)
            .build()

    companion object {
        private val logger by logger()

        // Per 2024-03-18 var det ingen verdier for andre språk enn norsk bokmål i kodeverkene vi bruker
        const val SPRAK_NORSK_BOKMAL = "nb"
    }
}

data class KodeverkDto(
    val betydninger: Map<String, List<BetydningDto>>,
) : Serializable

data class BetydningDto(
    val gyldigFra: LocalDate,
    val gyldigTil: LocalDate,
    val beskrivelser: Map<String, BeskrivelseDto>,
) : Serializable

data class BeskrivelseDto(
    val term: String,
    val tekst: String,
) : Serializable
