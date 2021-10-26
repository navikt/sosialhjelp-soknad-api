package no.nav.sosialhjelp.soknad.mock

import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils.isMockAltProfil
import no.nav.sosialhjelp.soknad.mock.MockAltFilterUtils.MOCK_ALT_API_HOST
import no.nav.sosialhjelp.soknad.mock.MockAltFilterUtils.isLocalhostMockAltApiRequest
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter

// filter for jax-rs clienter
class MockAltApiHostFilter : ClientRequestFilter {

    override fun filter(requestContext: ClientRequestContext?) {
        val mockAltApiHost = System.getenv(MOCK_ALT_API_HOST)
        if (isMockAltProfil() &&
            mockAltApiHost != null &&
            requestContext != null &&
            isLocalhostMockAltApiRequest(requestContext.uri)
        ) {
            val componentsBuilder = UriComponentsBuilder.fromUri(requestContext.uri)
            val uriString = componentsBuilder.host(mockAltApiHost).toUriString()
            requestContext.uri = UriComponentsBuilder.fromUriString(uriString).build().toUri()
        }
    }
}

// filter for webclient
class MockAltApiHostFilterWebClient : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val mockAltApiHost = System.getenv(MOCK_ALT_API_HOST)

        if (isMockAltProfil() &&
            mockAltApiHost != null &&
            isLocalhostMockAltApiRequest(request.url())
        ) {
            val componentsBuilder = UriComponentsBuilder.fromUri(request.url())
            val uriString = componentsBuilder.host(mockAltApiHost).toUriString()
            val filteredRequest = ClientRequest.from(request)
                .url(UriComponentsBuilder.fromUriString(uriString).build().toUri())
                .build()
            return next.exchange(filteredRequest)
        }
        return next.exchange(request)
    }
}

internal object MockAltFilterUtils {

    fun isLocalhostMockAltApiRequest(uri: URI): Boolean {
        return uri.host == LOCALHOST && uri.port == MOCK_ALT_API_PORT && uri.path.startsWith(MOCK_ALT_API_CONTEXT_PATH)
    }

    internal const val MOCK_ALT_API_HOST = "MOCK_ALT_API_HOST"
    private const val LOCALHOST = "localhost"
    private const val MOCK_ALT_API_PORT = 8989
    private const val MOCK_ALT_API_CONTEXT_PATH = "/sosialhjelp/mock-alt-api"
}
