package no.nav.sosialhjelp.soknad.auth.texas

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
import kotlin.getValue

@Component
class NonBlockingTexasClient(
    @param:Value("\${token_endpoint:null}") private val tokenEndpoint: String,
    @param:Value("\${token_exchange_endpoint:null}") private val tokenExchangeEndpoint: String,
    webClientBuilder: WebClient.Builder,
) {
    suspend fun getToken(
        identityProvider: String,
        target: String,
    ): TokenResponse {
        return doFetchToken(
            params = TokenRequestBody.GetRequest(identityProvider, target),
            endpoint = tokenEndpoint,
        )
    }

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
    ): TokenResponse {
        val response =
            try {
                texasWebClient
                    .post()
                    .uri(endpoint)
                    .body(BodyInserters.fromValue(params))
                    .retrieve()
                    .bodyToMono<TokenResponse.Success>()
                    .awaitSingleOrNull() ?: error("Empty response from Texas")
            } catch (e: WebClientResponseException) {
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
        return response
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
