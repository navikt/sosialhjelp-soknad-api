package no.nav.sosialhjelp.soknad.client.azure

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class AzureadConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${azure_token_endpoint}") val azureTokenEndpoint: String,
    @Value("\${azure_client_id}") val azureClientId: String,
    @Value("\${azure_client_secret}") val azureClientSecret: String,
    private val redisService: RedisService
) {

    @Bean
    open fun azureClient(): AzureadClient {
        return AzureadClient(azureWebClient, azureTokenEndpoint, azureClientSecret)
    }

    @Bean
    open fun azuredingsService(azureClient: AzureadClient): AzureadService {
        return AzureadService(azureClient, azureClientId, redisService)
    }

    private val azureWebClient: WebClient
        get() = proxiedWebClientBuilder.build()
}
