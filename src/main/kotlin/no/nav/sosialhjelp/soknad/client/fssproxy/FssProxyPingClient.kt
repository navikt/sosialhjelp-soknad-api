package no.nav.sosialhjelp.soknad.client.fssproxy

import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class FssProxyPingClient(
    @Value("\${fss_proxy_ping_url}") private val pingurl: String,
    webClientBuilder: WebClient.Builder,
) {

    private val fssProxyWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

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
