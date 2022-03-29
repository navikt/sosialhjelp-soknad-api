package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class BostotteConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${soknad.bostotte.url}") private val bostotteBaseUrl: String
) {

    @Bean
    open fun husbankenClient(): HusbankenClient {
        val husbankenClient = HusbankenClientImpl(husbankenWebClient)
        return MetricsFactory.createTimerProxy("HusbankenApi", husbankenClient, HusbankenClient::class.java)
    }

    @Bean
    open fun husbankenPing(husbankenClient: HusbankenClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata("$bostotteBaseUrl/ping", "HusbankenApi", false)
            try {
                husbankenClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val husbankenWebClient: WebClient
        get() = proxiedWebClientBuilder
            .baseUrl(bostotteBaseUrl)
            .build()
}
