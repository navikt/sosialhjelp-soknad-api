package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.client.config.proxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
        return HusbankenClientImpl(husbankenWebClient)
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

    private val husbankenWebClient: WebClient = proxiedWebClientBuilder(webClientBuilder, proxiedHttpClient)
        .baseUrl(bostotteBaseUrl)
        .build()
}
