package no.nav.sosialhjelp.soknad.client.oppslagapi

import no.nav.sosialhjelp.soknad.client.config.unproxiedHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppslagApiPingClient(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    webClientBuilder: WebClient.Builder,
) {
    private val pingurl = "${oppslagApiUrl}ping"

    private val oppslagApiWebClient: WebClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .build()

    fun ping() {
        oppslagApiWebClient.get()
            .uri(pingurl)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot sosialhjelp-oppslag-api feiler: ${it.message}", it)
            }
            .block()
    }
}
