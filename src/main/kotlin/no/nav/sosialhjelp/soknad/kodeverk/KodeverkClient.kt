package no.nav.sosialhjelp.soknad.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.resilience4j.retry.annotation.Retry
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavServiceHttpClient
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.Jackson2JsonDecoder
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
                .headers { headers ->
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    headers.add(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
                    headers.add(HEADER_CONSUMER_ID, getConsumerId())
                }
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
        configureWebClientBuilder(webClientBuilder, createNavServiceHttpClient())
            .codecs {
                it.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(
                        jacksonObjectMapper()
                            .registerModule(JavaTimeModule())
                            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY),
                    ),
                )
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
