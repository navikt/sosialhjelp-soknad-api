package no.nav.sosialhjelp.soknad.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SosialhjelpFssProxyCheck(
    @Value("\${fss_proxy_ping_url}") private val fssProxyPingUrl: String,
    webClientBuilder: WebClient.Builder,
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "sosialhjelp-fss-proxy (proxy for Aareg, Krr og Kodeverk)"
    override val address = fssProxyPingUrl
    override val importance = Importance.WARNING

    private val fssProxyWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun doCheck() {
        fssProxyWebClient.options()
            .uri(fssProxyPingUrl)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot sosialhjelp-fss-proxy feiler: ${it.message}", it)
            }
            .block()
    }
}
