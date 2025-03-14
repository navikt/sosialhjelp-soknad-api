package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.util.UUID

private const val QUERY_PARAMS = "?fra={fra}&til={til}"

class HusbankenClient(
    private val webClient: WebClient,
) {
    private val log by logger()

    fun hentBostotte(
        fra: LocalDate,
        til: LocalDate,
    ): BostotteDto {
        val token = SubjectHandlerUtils.getTokenOrNull()
        return webClient.get()
            .uri(QUERY_PARAMS, fra, til)
            .headers { headers -> token?.let { headers.add(HttpHeaders.AUTHORIZATION, "Bearer $it") } }
            .retrieve()
            .bodyToMono<BostotteDto>()
            .doOnSuccess { log.info("Hentet bostøtte informasjon fra Husbanken!") }
            .doOnError(WebClientResponseException::class.java) { e ->
                when {
                    e.statusCode.is4xxClientError -> log.error("Problemer med å koble opp mot Husbanken!", e)
                    e.statusCode.is5xxServerError ->
                        log.error(
                            "Problemer med å hente bostøtte fra Husbanken! Ekstern error: ${e.message}",
                            e,
                        )
                    else -> log.error("Problemer med å hente bostøtte informasjon fra Husbanken!", e)
                }
                throw HusbankenException("Feil vel henting av Bostotte fra Husbanken", e)
            }
            .block() ?: throw HusbankenException("Null ved henting fra Husbanken")
    }

    fun ping() {
        webClient.get()
            .uri("/ping")
            .retrieve()
            .bodyToMono<String>()
            .block()
    }
}

data class HusbankenException(
    val melding: String? = null,
    override val cause: Throwable? = null,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(melding, cause, soknadId.toString())
