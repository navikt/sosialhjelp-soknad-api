package no.nav.sosialhjelp.soknad.auth.azure

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AzureadClient(
    @Value("\${azure_token_endpoint}") val azureTokenEndpoint: String,
    @Value("\${azure_client_id}") val azureClientId: String,
    @Value("\${azure_client_secret}") val azureClientSecret: String,
    proxiedWebClientBuilder: WebClient.Builder,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val azureWebClient: WebClient = proxiedWebClientBuilder.build()

    suspend fun getSystemToken(scope: String): AzureadTokenResponse {
        return withContext(dispatcher) {
            azureWebClient
                .post()
                .uri(azureTokenEndpoint)
                .contentType(APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(systemTokenParams(scope)))
                .retrieve()
                .awaitBody()
        }
    }

    private fun systemTokenParams(scope: String): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "client_credentials")
        params.add("client_id", azureClientId)
        params.add("scope", "api://$scope/.default")
        params.add("client_secret", azureClientSecret)
        return params
    }
}
