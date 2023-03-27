package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class VirusScanConfig(
    @Value("\${virusscan_enabled}") private val enabled: Boolean,
    @Value("\${clamav_url}") private val clamAvUrl: String,
    webClientBuilder: WebClient.Builder,
) {

    @Bean
    fun virusScanner(): VirusScanner {
        return VirusScanner(virusScannerWebClient, enabled)
    }

    private val virusScannerWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(clamAvUrl).build()
}
