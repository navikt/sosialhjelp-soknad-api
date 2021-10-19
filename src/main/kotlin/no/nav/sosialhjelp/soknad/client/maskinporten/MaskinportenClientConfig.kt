package no.nav.sosialhjelp.soknad.client.maskinporten

import no.nav.sosialhjelp.metrics.MetricsFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Configuration
open class MaskinportenClientConfig(
    private val proxiedWebClient: WebClient,
    @Value("\${maskinporten_clientid}") private val clientId: String,
    @Value("\${maskinporten_scopes}") private val scopes: String,
    @Value("\${maskinporten_well_known_url}") private val wellKnownUrl: String,
    @Value("\${maskinporten_client_jwk}") private val clientJwk: String,
) {

    @Bean
    @Profile("!test")
    open fun maskinportenClient(): MaskinportenClient {
        val maskinportenClient = MaskinportenClientImpl(proxiedWebClient, maskinportenProperties, wellknown)
        return MetricsFactory.createTimerProxy("MaskinportenClient", maskinportenClient, MaskinportenClient::class.java)
    }

    @Bean
    @Profile("test")
    open fun maskinportenClientTest(): MaskinportenClient {
        return MaskinportenClientImpl(proxiedWebClient, maskinportenProperties, WellKnown("issuer", "token_url"))
    }

    private val wellknown: WellKnown
        get() = proxiedWebClient.get()
            .uri(wellKnownUrl)
            .retrieve()
            .bodyToMono<WellKnown>()
            .doOnSuccess { log.info("Hentet WellKnown for Maskinporten") }
            .doOnError { log.warn("Feil ved henting av WellKnown for Maskinporten", it) }
            .block()!!

    private val maskinportenProperties: MaskinportenProperties
        get() = MaskinportenProperties(
            clientId = clientId,
            jwkPrivate = clientJwk,
            scope = scopes,
            wellKnownUrl = wellKnownUrl
        )

    companion object {
        private val log = LoggerFactory.getLogger(MaskinportenClientConfig::class.java)
    }
}

data class WellKnown(
    val issuer: String,
    val token_endpoint: String
)
