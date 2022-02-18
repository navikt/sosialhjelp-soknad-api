package no.nav.sosialhjelp.soknad.client.oppslagapi

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class OppslagApiConfig(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    private val nonProxiedWebClientBuilder: WebClient.Builder,
) {

    @Bean
    open fun oppslagApiPingClient(): OppslagApiPingClient {
        val pingurl = "${oppslagApiUrl}ping"
        return OppslagApiPingClient(oppslagApiWebClient, pingurl)
    }

    @Bean
    open fun oppslagApiPing(oppslagApiPingClient: OppslagApiPingClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(oppslagApiUrl, "sosialhjelp-oppslag-api", false)
            try {
                oppslagApiPingClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val oppslagApiWebClient: WebClient
        get() = nonProxiedWebClientBuilder.build()
}
