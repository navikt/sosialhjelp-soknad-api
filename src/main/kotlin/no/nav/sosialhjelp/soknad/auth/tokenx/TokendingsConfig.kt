package no.nav.sosialhjelp.soknad.auth.tokenx

import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.tokenx.JwtProviderUtil.downloadWellKnown
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class TokendingsConfig(
    @Value("\${tokendings_url}") val tokendingsUrl: String,
    @Value("\${tokendings_client_id}") val tokendingsClientId: String,
    @Value("\${tokendings_private_jwk}") val tokendingsPrivateJwk: String,
    private val redisService: RedisService,
    webClientBuilder: WebClient.Builder,
) {

    @Profile("!test")
    @Bean
    fun tokendingsClient(): TokendingsClient {
        return TokendingsClientImpl(tokendingsWebClient, wellKnown)
    }

    @Profile("test")
    @Bean
    fun tokendingsClientTest(): TokendingsClient {
        return TokendingsClientImpl(
            tokendingsWebClient,
            WellKnown("iss-localhost", "authorizationEndpoint", "tokenEndpoint", tokendingsUrl),
        )
    }

    @Bean
    fun tokendingsService(tokendingsClient: TokendingsClient): TokendingsService {
        return TokendingsService(
            tokendingsClient,
            tokendingsClientId,
            tokendingsPrivateJwk,
            redisService,
        )
    }

    private val tokendingsWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

    private val wellKnown: WellKnown
        get() = downloadWellKnown(tokendingsUrl)
}
