package no.nav.sosialhjelp.soknad.maskinporten

import no.nav.sosialhjelp.metrics.MetricsFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class MaskinportenClientConfig(
    private val proxiedWebClient: WebClient
) {

    // todo implement

    @Bean
    open fun maskinportenClient(): MaskinportenClient {
        val maskinportenClient = MaskinportenClientImpl(webClient = proxiedWebClient)
        return MetricsFactory.createTimerProxy("MaskinportenClient", maskinportenClient, MaskinportenClient::class.java)
    }
}