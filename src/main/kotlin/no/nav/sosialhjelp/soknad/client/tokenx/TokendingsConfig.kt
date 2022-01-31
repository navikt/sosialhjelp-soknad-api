package no.nav.sosialhjelp.soknad.client.tokenx

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import no.nav.sosialhjelp.soknad.client.config.unproxiedHttpClient
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.JwtProviderUtil.downloadWellKnown
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class TokendingsConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${tokendings_url}") val tokendingsUrl: String,
    @Value("\${tokendings_client_id}") val tokendingsClientId: String,
    @Value("\${tokendings_private_jwk}") val tokendingsPrivateJwk: String,
    private val redisService: RedisService
) {

    @Profile("!test")
    @Bean
    open fun tokendingsClient(): TokendingsClient {
        return TokendingsClientImpl(tokendingsWebClient, wellKnown)
    }

    @Profile("test")
    @Bean
    open fun tokendingsClientTest(): TokendingsClient {
        return TokendingsClientImpl(
            tokendingsWebClient,
            WellKnown("iss-localhost", "authorizationEndpoint", "tokenEndpoint", tokendingsUrl)
        )
    }

    @Bean
    open fun tokendingsService(tokendingsClient: TokendingsClient): TokendingsService {
        return TokendingsService(
            tokendingsClient,
            tokendingsClientId,
            tokendingsPrivateJwk,
            redisService
        )
    }

    private val tokendingsWebClient: WebClient
        get() = proxiedWebClientBuilder
            .clientConnector(
                ReactorClientHttpConnector(
                    unproxiedHttpClient()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                        .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(60)) }
                )
            ).build()

    private val wellKnown: WellKnown
        get() = downloadWellKnown(tokendingsUrl)
}
