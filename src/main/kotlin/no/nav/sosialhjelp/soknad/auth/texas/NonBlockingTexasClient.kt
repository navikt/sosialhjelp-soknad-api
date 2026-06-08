package no.nav.sosialhjelp.soknad.auth.texas

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotlinx.coroutines.reactor.awaitSingleOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.filter.MdcExchangeFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NonBlockingTexasClient(
    @param:Value("\${token_endpoint:null}") private val tokenEndpoint: String,
    @param:Value("\${token_exchange_endpoint:null}") private val tokenExchangeEndpoint: String,
    webClientBuilder: WebClient.Builder,
) {
    @WithSpan("NBC - Get Token from Texas")
    suspend fun getToken(
        identityProvider: String,
        target: String,
    ): TokenResponse {
        return doFetchToken(
            params = TokenRequestBody.GetRequest(identityProvider, target),
            endpoint = tokenEndpoint,
        )
    }

    @WithSpan("NBC - Exchange Token from Texas")
    suspend fun exchangeToken(
        identityProvider: String,
        target: String,
        userToken: String,
    ): TokenResponse {
        return doFetchToken(
            params =
                TokenRequestBody.ExchangeRequest(
                    identityProvider = identityProvider,
                    target = target,
                    userToken = userToken,
                ),
            endpoint = tokenExchangeEndpoint,
        )
    }

    private suspend fun doFetchToken(
        params: TokenRequestBody,
        endpoint: String,
    ): TokenResponse =
        runCatching {
            texasWebClient
                .post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(params))
                .retrieve()
                .bodyToMono<TokenResponse.Success>()
                .awaitSingleOrNull() ?: error("Empty response from Texas")
        }
            .getOrElse { e ->
                Span.current().recordException(e)
                Span.current().setStatus(StatusCode.ERROR)

                if (e !is WebClientResponseException) {
                    throw e
                } else {
                    val error = e.responseBodyAsString
                    logger.error("Failed to fetch token from Texas: $error")
                    TokenResponse.Error(
                        error =
                            TokenErrorResponse(
                                "Unknown error: ${e.statusCode}",
                                e.statusText,
                            ),
                        errorDescription = e.responseBodyAsString,
                    )
                }
            }

    private val texasWebClient: WebClient =
        webClientBuilder
            .filter(MdcExchangeFilter)
            .defaultHeaders { it.contentType = MediaType.APPLICATION_JSON }
            .build()

    companion object {
        private val logger by logger()
    }
}
