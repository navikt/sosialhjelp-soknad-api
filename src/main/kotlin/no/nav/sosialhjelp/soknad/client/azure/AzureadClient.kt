package no.nav.sosialhjelp.soknad.client.azure

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class AzureadClient(
    private val azureWebClient: WebClient,
    private val azureTokenEndpoint: String,
    private val azureClientId: String,
    private val azureClientSecret: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

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
