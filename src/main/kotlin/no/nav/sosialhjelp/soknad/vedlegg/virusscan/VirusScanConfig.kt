package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import no.nav.sosialhjelp.soknad.client.config.unproxiedHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class VirusScanConfig(
    @Value("\${virusscan_enabled}") private val enabled: Boolean,
    @Value("\${clamav_url}") private val clamAvUrl: String,
    webClientBuilder: WebClient.Builder,
) {

    @Bean
    open fun virusScanner(): VirusScanner {
        return VirusScanner(virusScannerWebClient, enabled)
    }

    private val virusScannerWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .baseUrl(clamAvUrl)
            .build()
}
