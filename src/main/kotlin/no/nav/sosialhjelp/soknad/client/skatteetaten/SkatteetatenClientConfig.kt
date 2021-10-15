package no.nav.sosialhjelp.soknad.client.skatteetaten

import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class SkatteetatenClientConfig(
    private val proxiedWebClient: WebClient,
    @Value("\${skatteetaten_api_baseurl}") private val baseurl: String,
    private val maskinportenClient: MaskinportenClient
) {

    @Bean
    open fun skatteetatenClient(): SkatteetatenClient {
        val skatteetatenClient = SkatteetatenClientImpl(skatteetatenWebClient, maskinportenClient)
        return createTimerProxy("SkatteetatenApi", skatteetatenClient, SkatteetatenClient::class.java)
    }

    private val skatteetatenWebClient: WebClient
        get() = proxiedWebClient.mutate()
            .baseUrl(baseurl)
            .build()
}
