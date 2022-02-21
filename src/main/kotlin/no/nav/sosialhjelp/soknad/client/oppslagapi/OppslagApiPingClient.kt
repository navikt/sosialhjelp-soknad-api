package no.nav.sosialhjelp.soknad.client.oppslagapi

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

class OppslagApiPingClient(
    private val oppslagApiWebClient: WebClient,
    private val pingurl: String
) {
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
