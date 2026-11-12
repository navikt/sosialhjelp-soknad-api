package no.nav.sosialhjelp.soknad.app.security.texas

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.filter.MdcExchangeFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class TexasClient(
    @param:Value("\${token_endpoint:null}") private val tokenEndpoint: String,
    @param:Value("\${token_exchange_endpoint:null}") private val tokenExchangeEndpoint: String,
    @param:Value("\${token_introspection_endpoint:null}") private val introspectionEndpoint: String,
    webClientBuilder: WebClient.Builder,
) {
    fun getToken(
        identityProvider: String,
        target: String,
    ): TokenResponse {
        return doFetchToken<TokenResponse.Success>(
            params = TokenRequestBody.GetRequest(identityProvider, target),
            endpoint = tokenEndpoint,
        )
    }

    fun exchangeToken(
        identityProvider: String,
        target: String,
        userToken: String,
    ): TokenResponse {
        return doFetchToken<TokenResponse.Success>(
            params =
                TokenRequestBody.ExchangeRequest(
                    identityProvider = identityProvider,
                    target = target,
                    userToken = userToken,
                ),
            endpoint = tokenExchangeEndpoint,
        )
    }

    fun validateToken(
        identityProvider: String,
        token: String,
    ): TokenResponse {
        return doFetchToken<TokenResponse.ValidationResponse>(
            params = TokenRequestBody.ValidateRequest(identityProvider, token),
            endpoint = introspectionEndpoint,
        )
    }

    private inline fun <reified T : TokenResponse>doFetchToken(
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
                    .bodyToMono(T::class.java)
                    .block() ?: error("Empty response from Texas")
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

sealed interface TokenRequestBody {
    data class GetRequest(
        @param:JsonProperty("identity_provider")
        val identityProvider: String,
        val target: String,
    ) : TokenRequestBody

    data class ExchangeRequest(
        @param:JsonProperty("identity_provider")
        val identityProvider: String,
        val target: String,
        @param:JsonProperty("user_token")
        val userToken: String,
    ) : TokenRequestBody

    data class ValidateRequest(
        @param:JsonProperty("identity_provider")
        val identityProvider: String,
        val token: String,
    ) : TokenRequestBody
}

sealed interface TokenResponse {
    data class Success(
        @param:JsonProperty("access_token")
        val token: String,
        @param:JsonProperty("expires_in")
        val expiresInSeconds: Int,
    ) : TokenResponse

    data class ValidationResponse(
        val active: Boolean,
    ) : TokenResponse {
        val claims: MutableMap<String, Any?> = mutableMapOf()

        @JsonAnySetter
        fun addClaim(
            name: String,
            value: Any?,
        ) {
            claims[name] = value
        }
    }

    data class Error(
        val error: TokenErrorResponse,
        @param:JsonProperty("error_description")
        val errorDescription: String,
    ) : TokenResponse
}

data class TokenErrorResponse(
    val error: String,
    @param:JsonProperty("error_description")
    val errorDescription: String,
)
