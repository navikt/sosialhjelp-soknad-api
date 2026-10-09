package no.nav.sosialhjelp.soknad.inntekt.husbanken

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
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
    suspend fun getBostotte(
        fra: LocalDate = LocalDate.now().minusDays(60),
        til: LocalDate = LocalDate.now(),
    ): HusbankenResponse = doGet(fra, til)

    private suspend fun doGet(
        fra: LocalDate,
        til: LocalDate,
    ): HusbankenResponse {
        return withContext(Dispatchers.IO) {
            webClient.get()
                .uri(QUERY_PARAMS, fra, til)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .retrieve()
                .bodyToMono<BostotteDto>()
                .map<HusbankenResponse> { dto -> HusbankenResponse.Success(dto) }
                .onErrorResume(WebClientResponseException::class.java) { e -> Mono.just(HusbankenResponse.Error(e)) }
                .awaitSingleOrNull() ?: HusbankenResponse.Null
        }
    }

    private suspend fun authorizationHeader(): String = "Bearer ${currentUserContext().userToken}"
}

sealed interface HusbankenResponse {
    data class Success(val bostotte: BostotteDto) : HusbankenResponse

    data class Error(val e: WebClientResponseException) : HusbankenResponse

    object Null : HusbankenResponse
}
