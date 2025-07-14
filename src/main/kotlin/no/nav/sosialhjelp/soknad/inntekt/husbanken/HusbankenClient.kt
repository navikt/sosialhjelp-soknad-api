package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate

private const val QUERY_PARAMS = "?fra={fra}&til={til}"

class HusbankenClient(
    private val webClient: WebClient,
) {
    fun getBostotte(
        fra: LocalDate = LocalDate.now().minusDays(60),
        til: LocalDate = LocalDate.now(),
    ): HusbankenResponse = doGet(fra, til)

    private fun doGet(
        fra: LocalDate,
        til: LocalDate,
    ): HusbankenResponse {
        return webClient.get()
            .uri(QUERY_PARAMS, fra, til)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${userToken()}")
            .retrieve()
            .bodyToMono<BostotteDto>()
            .map<HusbankenResponse> { dto -> HusbankenResponse.Success(dto) }
            .onErrorResume(WebClientResponseException::class.java) { e -> Mono.just(HusbankenResponse.Error(e)) }
            .block()
            ?: HusbankenResponse.Null
    }

    private fun userToken() = SubjectHandlerUtils.getTokenOrNull() ?: error("Token is null, kan ikke kalle Husbanken")
}

sealed interface HusbankenResponse {
    data class Success(val bostotte: BostotteDto) : HusbankenResponse

    data class Error(val e: WebClientResponseException) : HusbankenResponse

    object Null : HusbankenResponse
}
