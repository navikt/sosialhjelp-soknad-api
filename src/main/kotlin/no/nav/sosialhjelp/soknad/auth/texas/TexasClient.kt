package no.nav.sosialhjelp.soknad.auth.texas

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class TexasService(val texasClient: TexasClient) {
    fun getToken(
        idProvider: String,
        target: String,
    ): String {
        runCatching {
//            val params = mapOf("identity_provider" to idProvider, "target" to target)

            return TokenRequestBody(idProvider, target)
                .let {
                    when (val response = texasClient.fetchToken(it)) {
                        is TokenResponse.Success -> response.token
                        is TokenResponse.Error -> throw IllegalStateException("Failed to fetch token from Texas: $response")
                    }
                }
        }
            .onSuccess { logger.info("Successfully fetched token from Texas") }
            .onFailure { e -> logger.error("Failed to fetch token from Texas", e) }
        return ""
    }

    companion object {
        private val logger by logger()
    }
}

@Component
class TexasClient(
    @Value("\${nais_token_endpoint:null}") private val tokenEndpoint: String,
    webClientBuilder: WebClient.Builder,
) {
    private val texasWebClient: WebClient =
        webClientBuilder
            .baseUrl(tokenEndpoint)
            .defaultHeaders { it.contentType = MediaType.APPLICATION_JSON }
            .build()

    fun fetchToken(params: TokenRequestBody): TokenResponse {
        return texasWebClient
            .post()
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .block() ?: throw IllegalStateException("Failed to fetch token from Texas")
    }
}

data class TokenRequestBody(
    @JsonProperty("identity_provider")
    val idProvider: String,
    val target: String,
)

sealed class TokenResponse {
    data class Success(
        @JsonProperty("access_token")
        val token: String,
        @JsonProperty("expires_in")
        val expiresInSeconds: Int,
    ) : TokenResponse()

    data class Error(
        val error: TokenErrorResponse,
        @JsonProperty("error_description")
        val errorDescription: String,
    ) : TokenResponse()
}

data class TokenErrorResponse(
    val error: String,
    @JsonProperty("error_description")
    val errorDescription: String,
)
