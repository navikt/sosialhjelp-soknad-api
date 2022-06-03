package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

interface HusbankenClient {
    fun hentBostotte(token: String?, fra: LocalDate, til: LocalDate): BostotteDto?
    fun ping()
}

class HusbankenClientImpl(
    private val webClient: WebClient
) : HusbankenClient {

    override fun hentBostotte(token: String?, fra: LocalDate, til: LocalDate): BostotteDto? {
        return try {
            webClient.get()
                .uri(QUERYPARAMS, fra, til)
                .headers { headers -> token?.let { headers.add(HttpHeaders.AUTHORIZATION, it) } }
                .retrieve()
                .bodyToMono<BostotteDto>()
                .doOnSuccess { log.info("Hentet bostøtte informasjon fra Husbanken!") }
                .doOnError(WebClientResponseException::class.java) { e ->
                    when {
                        e.statusCode.is4xxClientError -> log.error("Problemer med å koble opp mot Husbanken!", e)
                        e.statusCode.is5xxServerError -> log.error("Problemer med å hente bostøtte fra Husbanken! Ekstern error: {}", e.message, e)
                        else -> log.error("Problemer med å hente bostøtte informasjon fra Husbanken!", e)
                    }
                }
                .block()
        } catch (e: Exception) {
            return null
        }
    }

    override fun ping() {
        webClient.get()
            .uri { it.path("/ping").build() }
            .retrieve()
            .bodyToMono<String>()
            .block()
    }

    companion object {
        private val log = getLogger(HusbankenClientImpl::class.java)
        private const val QUERYPARAMS = "?fra={fra}&til={til}"
    }
}
