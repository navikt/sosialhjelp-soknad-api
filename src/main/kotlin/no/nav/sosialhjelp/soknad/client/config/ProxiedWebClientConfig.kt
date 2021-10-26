package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import no.nav.sosialhjelp.soknad.mock.MockAltApiHostFilterWebClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

@Profile("!(mock-alt|test)")
@Configuration
open class ProxiedWebClientConfig(
    @Value("\${HTTPS_PROXY}") private val proxyUrl: String
) {

    @Bean
    open fun proxiedWebClient(): WebClient =
        WebClient.builder()
            .clientConnector(getProxiedReactorClientHttpConnector(proxyUrl))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()

    private fun getProxiedReactorClientHttpConnector(proxyUrl: String): ReactorClientHttpConnector {
        val uri = URL(proxyUrl)

        val httpClient: HttpClient = HttpClient.create()
            .resolver(DefaultAddressResolverGroup.INSTANCE)
            .proxy { proxy ->
                proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
            }

        return ReactorClientHttpConnector(httpClient)
    }
}

@Profile("(mock-alt|test)")
@Configuration
open class MockProxiedWebClientConfig {

    @Bean
    open fun proxiedWebClient(): WebClient =
        WebClient.builder()
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .filter(MockAltApiHostFilterWebClient())
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()

    private fun getUnproxiedReactorClientHttpConnector(): ReactorClientHttpConnector {
        val httpClient: HttpClient = HttpClient
            .newConnection()
            .resolver(DefaultAddressResolverGroup.INSTANCE)
        return ReactorClientHttpConnector(httpClient)
    }
}
