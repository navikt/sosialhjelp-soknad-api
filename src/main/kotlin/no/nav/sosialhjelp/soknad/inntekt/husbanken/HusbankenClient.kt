package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

class HusbankenClient(
    private val webClient: WebClient,
) {

    fun hentBostotte(token: String?, fra: LocalDate, til: LocalDate): BostotteDto? {
        return try {
            webClient.get()
                .uri(QUERY_PARAMS, fra, til)
                .headers { headers -> token?.let { headers.add(HttpHeaders.AUTHORIZATION, it) } }
                .retrieve()
                .bodyToMono<BostotteDto>()
                .doOnSuccess { log.info("Hentet bostøtte informasjon fra Husbanken!") }
                .doOnError(WebClientResponseException::class.java) { e ->
                    when {
                        e.statusCode.is4xxClientError -> log.error("Problemer med å koble opp mot Husbanken!", e)
                        e.statusCode.is5xxServerError -> log.error("Problemer med å hente bostøtte fra Husbanken! Ekstern error: ${e.message}", e)
                        else -> log.error("Problemer med å hente bostøtte informasjon fra Husbanken!", e)
                    }
                }
                .block()
        } catch (e: Exception) {
            log.warn("hentBostotte fra Husbanken kastet feil", e)
            return null
        }
    }

    fun ping() {
        webClient.get()
            .uri("/ping")
            .retrieve()
            .bodyToMono<String>()
            .block()
    }

    companion object {
        private val log by logger()
        private const val QUERY_PARAMS = "?fra={fra}&til={til}"
    }
}
