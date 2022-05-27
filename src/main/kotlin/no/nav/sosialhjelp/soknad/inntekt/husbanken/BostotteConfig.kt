package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
open class BostotteConfig(
    @Value("\${soknad.bostotte.url}") private val bostotteBaseUrl: String,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
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

    private val husbankenWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .baseUrl(bostotteBaseUrl)
            .build()
}
