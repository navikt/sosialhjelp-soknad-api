package no.nav.sosialhjelp.soknad.client.azure

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
) {

    @Bean
    open fun azureClient(): AzureadClient {
        return AzureadClient(azureWebClient, azureTokenEndpoint, azureClientId, azureClientSecret)
    }

    private val azureWebClient: WebClient
        get() = proxiedWebClientBuilder.build()
}
