package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class VirusScanConfig(
    private val nonProxiedWebClientBuilder: WebClient.Builder,
    @Value("\${virusscan_enabled}") private val enabled: Boolean,
    @Value("\${clamav_url}") private val clamAvUrl: String
) {

    @Bean
    open fun virusScanner(): VirusScanner {
        return VirusScanner(virusScannerWebClient, enabled)
    }

    private val virusScannerWebClient: WebClient
        get() = nonProxiedWebClientBuilder.baseUrl(clamAvUrl).build()
}
