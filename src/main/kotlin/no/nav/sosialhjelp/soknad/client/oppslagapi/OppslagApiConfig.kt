package no.nav.sosialhjelp.soknad.client.oppslagapi

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OppslagApiConfig(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    private val oppslagApiPingClient: OppslagApiPingClient,
) {
    private val pingurl = "${oppslagApiUrl}ping"

    @Bean
    open fun oppslagApiPing(): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(pingurl, "sosialhjelp-oppslag-api", false)
            try {
                oppslagApiPingClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
