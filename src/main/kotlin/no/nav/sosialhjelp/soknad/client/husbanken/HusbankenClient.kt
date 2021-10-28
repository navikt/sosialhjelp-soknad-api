package no.nav.sosialhjelp.soknad.client.husbanken

import no.nav.sosialhjelp.soknad.client.husbanken.dto.BostotteDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.util.Optional

interface HusbankenClient {
    fun hentBostotte(token: String, fra: LocalDate, til: LocalDate): Optional<BostotteDto>
    fun ping()
}

class HusbankenClientImpl(
    private val webClient: WebClient
) : HusbankenClient {

    override fun hentBostotte(token: String, fra: LocalDate, til: LocalDate): Optional<BostotteDto> {
        return webClient.get()
            .uri { it.queryParam("fra", fra).queryParam("til", til).build() }
            .headers { it.setBearerAuth(token) }
            .retrieve()
            .bodyToMono<BostotteDto>()
            .doOnError(WebClientResponseException::class.java) { e ->
                when {
                    e.statusCode.is4xxClientError -> log.error("Problemer med å koble opp mot Husbanken!", e)
                    e.statusCode.is5xxServerError -> log.error("Problemer med å hente bostøtte fra Husbanken! Ekstern error: {}", e.message, e)
                    else -> log.error("Problemer med å hente bostøtte informasjon fra Husbanken!", e)
                }
            }
            .blockOptional()
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
    }
}
