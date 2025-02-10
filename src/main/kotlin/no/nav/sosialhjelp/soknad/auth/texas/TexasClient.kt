package no.nav.sosialhjelp.soknad.auth.texas

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TexasClient(
    @Value("\${token_endpoint:null}") private val tokenEndpoint: String,
    @Value("\${token_exchange_endpoint:null}") private val tokenExchangeEndpoint: String,
    webClientBuilder: WebClient.Builder,
) {
    fun getToken(
        identityProvider: String,
        target: String,
    ): TokenResponse {
        return doFetchToken(
            params = TokenRequestBody.GetRequest(identityProvider, target),
            endpoint = tokenEndpoint,
        )
    }

    fun exchangeToken(
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

    private fun doFetchToken(
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
                    .block()
            } catch (e: WebClientResponseException) {
                val error = e.responseBodyAsString
                logger.error("Failed to fetch token from Texas: $error")
                TokenResponse.Error(
                    error =
                        TokenErrorResponse(
                            "Unknown error: ${e.statusCode}",
                            e.statusText ?: "Unknown error",
                        ),
                    errorDescription = e.responseBodyAsString,
                )
            }
        return response
    }

    private val texasWebClient: WebClient =
        webClientBuilder
            .defaultHeaders { it.contentType = MediaType.APPLICATION_JSON }
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()

    companion object {
        private val logger by logger()
    }
}

sealed interface TokenRequestBody {
    data class GetRequest(
        @JsonProperty("identity_provider")
        val identityProvider: String,
        val target: String,
    ) : TokenRequestBody

    data class ExchangeRequest(
        @JsonProperty("identity_provider")
        val identityProvider: String,
        val target: String,
        @JsonProperty("user_token")
        val userToken: String,
    ) : TokenRequestBody
}

sealed interface TokenResponse {
    data class Success(
        @JsonProperty("access_token")
        val token: String,
        @JsonProperty("expires_in")
        val expiresInSeconds: Int,
    ) : TokenResponse

    data class Error(
        val error: TokenErrorResponse,
        @JsonProperty("error_description")
        val errorDescription: String,
    ) : TokenResponse
}

data class TokenErrorResponse(
    val error: String,
    @JsonProperty("error_description")
    val errorDescription: String,
)

private val objectMapper =
    jacksonObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
