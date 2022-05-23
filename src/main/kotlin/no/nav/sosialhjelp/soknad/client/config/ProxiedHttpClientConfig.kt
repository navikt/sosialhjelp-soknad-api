package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

@Profile("!(mock-alt|test)")
@Configuration
open class ProxiedHttpClientConfig(
    @Value("\${HTTPS_PROXY}") private val proxyUrl: String
) {
    @Bean
    open fun proxiedHttpClient(): HttpClient = proxiedHttpClient(proxyUrl)

    private fun proxiedHttpClient(proxyUrl: String): HttpClient {
        val uri = URL(proxyUrl)

        return HttpClient.create()
            .resolver(DefaultAddressResolverGroup.INSTANCE)
            .proxy { proxy ->
                proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
            }
    }
}

@Profile("(mock-alt|test)")
@Configuration
open class MockProxiedHttpClientConfig {

    @Bean
    open fun proxiedHttpClient(): HttpClient = unproxiedHttpClient()
}
