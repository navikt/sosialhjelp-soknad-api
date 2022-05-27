package no.nav.sosialhjelp.soknad.auth.maskinporten

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
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
        val maskinportenClient = MaskinportenClientImpl(maskinPortenWebClient, maskinportenProperties, wellknown)
        return MetricsFactory.createTimerProxy("MaskinportenClient", maskinportenClient, MaskinportenClient::class.java)
    }

    @Bean
    @Profile("test")
    open fun maskinportenClientTest(): MaskinportenClient {
        return MaskinportenClientImpl(maskinPortenWebClient, maskinportenProperties, WellKnown("issuer", "token_url"))
    }

    private val maskinPortenWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()

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
