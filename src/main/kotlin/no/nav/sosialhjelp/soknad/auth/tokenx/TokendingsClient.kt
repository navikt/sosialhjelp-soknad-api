package no.nav.sosialhjelp.soknad.auth.tokenx

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

interface TokendingsClient {
    suspend fun exchangeToken(subjectToken: String, clientAssertion: String, audience: String): TokendingsResponse
    val audience: String
}

class TokendingsClientImpl(
    private val tokendingsWebClient: WebClient,
    val wellKnown: WellKnown,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TokendingsClient {

    override suspend fun exchangeToken(subjectToken: String, clientAssertion: String, audience: String): TokendingsResponse {
        return withContext(dispatcher) {
            val params = LinkedMultiValueMap<String, String>()
            params.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            params.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            params.add("client_assertion", clientAssertion)
            params.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
            params.add("subject_token", subjectToken)
            params.add("audience", audience)

            tokendingsWebClient
                .post()
                .uri(wellKnown.tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .awaitBody()
        }
    }

    override val audience: String
        get() = wellKnown.tokenEndpoint
}
