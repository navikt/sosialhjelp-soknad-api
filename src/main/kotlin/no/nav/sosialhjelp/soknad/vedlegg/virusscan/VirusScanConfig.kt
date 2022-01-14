package no.nav.sosialhjelp.soknad.vedlegg.virusscan

import no.nav.sosialhjelp.soknad.common.ServiceUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class VirusScanConfig(
    private val nonProxiedWebClientBuilder: WebClient.Builder,
    @Value("\${soknad.vedlegg.virusscan.enabled}") private val enabled: Boolean,
    private val serviceUtils: ServiceUtils
) {

    @Bean
    open fun virusScanner(): VirusScanner {
        return ClamAvVirusScanner(virusScannerWebClient, enabled, serviceUtils)
    }

    private val virusScannerWebClient: WebClient
        get() = nonProxiedWebClientBuilder.baseUrl(DEFAULT_CLAM_URI).build()

    companion object {
        internal const val DEFAULT_CLAM_URI = "http://clamav.nais.svc.nais.local/scan"
    }
}
