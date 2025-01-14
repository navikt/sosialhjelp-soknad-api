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
            return when (val fetchToken = texasClient.fetchToken(TokenRequest(idProvider, target))) {
                is TokenResponse.Success -> fetchToken.token
                is TokenResponse.Error -> throw IllegalStateException("Failed to fetch token from Texas: ${fetchToken.errorDescription}")
            }
        }
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
            .build()

    fun fetchToken(request: TokenRequest): TokenResponse {
        return texasWebClient
            .post()
            .uri(tokenEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .block() ?: throw IllegalStateException("Failed to fetch token from Texas")
    }
}

data class TokenRequest(
    @JsonProperty("id_provider")
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
