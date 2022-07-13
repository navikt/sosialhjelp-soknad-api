package no.nav.sosialhjelp.soknad.auth.maskinporten

import no.nav.sosialhjelp.soknad.client.config.proxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient

@Configuration
open class MaskinportenClientConfig(
    @Value("\${maskinporten_clientid}") private val clientId: String,
    @Value("\${maskinporten_scopes}") private val scopes: String,
    @Value("\${maskinporten_well_known_url}") private val wellKnownUrl: String,
    @Value("\${maskinporten_client_jwk}") private val clientJwk: String,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {

    @Bean
    @Profile("!test")
    open fun maskinportenClient(): MaskinportenClient {
        return MaskinportenClientImpl(maskinPortenWebClient, maskinportenProperties, wellknown)
    }

    @Bean
    @Profile("test")
    open fun maskinportenClientTest(): MaskinportenClient {
        return MaskinportenClientImpl(maskinPortenWebClient, maskinportenProperties, WellKnown("issuer", "token_url"))
    }

    private val maskinPortenWebClient: WebClient = proxiedWebClientBuilder(webClientBuilder, proxiedHttpClient).build()

    private val wellknown: WellKnown
        get() = maskinPortenWebClient.get()
            .uri(wellKnownUrl)
            .retrieve()
            .bodyToMono<WellKnown>()
            .doOnSuccess { log.info("Hentet WellKnown for Maskinporten") }
            .doOnError { log.warn("Feil ved henting av WellKnown for Maskinporten", it) }
            .block() ?: throw TjenesteUtilgjengeligException("Feil ved henting av WellKnown for Maskinporten", null)

    private val maskinportenProperties = MaskinportenProperties(
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
    val token_endpoint: String,
)
