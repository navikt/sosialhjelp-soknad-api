package no.nav.sosialhjelp.soknad.client.oppslagapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppslagApiPingClient(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    nonProxiedWebClientBuilder: WebClient.Builder,
) {
    private val pingurl = "${oppslagApiUrl}ping"

    private val oppslagApiWebClient: WebClient = nonProxiedWebClientBuilder.build()

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
