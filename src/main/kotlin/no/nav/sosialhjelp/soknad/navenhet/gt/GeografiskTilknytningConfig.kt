package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GeografiskTilknytningConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    private val geografiskTilknytningClient: GeografiskTilknytningClient
) {

    @Bean
    open fun pdlPing(): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "PDL", true)
            try {
                geografiskTilknytningClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
