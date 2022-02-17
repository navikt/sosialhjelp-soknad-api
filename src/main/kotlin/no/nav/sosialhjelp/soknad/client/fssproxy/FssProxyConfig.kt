package no.nav.sosialhjelp.soknad.client.fssproxy

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class FssProxyConfig(
    @Value("\${fss_proxy_ping_url}") private val pingurl: String,
    private val nonProxiedWebClientBuilder: WebClient.Builder,
) {

    @Bean
    open fun fssProxyPingClient(): FssProxyPingClient {
        return FssProxyPingClient(fssProxyWebClient, pingurl)
    }

    @Bean
    open fun fssProxyPing(fssProxyPingClient: FssProxyPingClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(pingurl, "sosialhjelp-fss-proxy", false)
            try {
                fssProxyPingClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val fssProxyWebClient: WebClient
        get() = nonProxiedWebClientBuilder.build()
}
