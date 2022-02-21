package no.nav.sosialhjelp.soknad.client.fssproxy

import no.nav.sosialhjelp.kotlin.utils.logger
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

class FssProxyPingClient(
    private val fssProxyWebClient: WebClient,
    private val pingurl: String
) {
    fun ping() {
        fssProxyWebClient.options()
            .uri(pingurl)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot sosialhjelp-fss-proxy feiler: ${it.message}", it)
            }
            .block()
    }

    companion object {
        private val log by logger()
    }
}
