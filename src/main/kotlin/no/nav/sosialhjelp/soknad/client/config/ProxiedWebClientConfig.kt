package no.nav.sosialhjelp.soknad.client.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Profile("!(mock-alt|test)")
@Configuration
open class ProxiedWebClientConfig(
    @Value("\${HTTPS_PROXY}") private val proxyUrl: String
) {

    @Bean
    open fun proxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getProxiedReactorClientHttpConnector(proxyUrl))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
}

@Profile("(mock-alt|test)")
@Configuration
open class MockProxiedWebClientConfig {

    @Bean
    open fun proxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
}

@Configuration
open class NonProxiedWebClientConfig {

    @Bean
    open fun nonProxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
}
