package no.nav.sosialhjelp.soknad.client.idporten

import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenAccessTokenResponse
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import no.nav.sosialhjelp.idporten.client.IdPortenClientImpl
import no.nav.sosialhjelp.idporten.client.IdPortenProperties
import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Profile("!mock-alt")
@Configuration
open class IdPortenClientConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${idporten_token_url}") private val tokenUrl: String,
    @Value("\${idporten_clientid}") private val clientId: String,
    @Value("\${idporten_scope}") private val scope: String,
    @Value("\${idporten_config_url}") private val configUrl: String,
    @Value("\${virksomhetssertifikat_path}") private val virksomhetssertifikatPath: String,
) {

    @Bean
    open fun idPortenClient(): IdPortenClient {
        val idPortenClient = IdPortenClientImpl(idPortenWebClient, idPortenProperties)
        return createTimerProxy("IdPortenClient", idPortenClient, IdPortenClient::class.java)
    }

    private val idPortenWebClient: WebClient
        get() = proxiedWebClientBuilder.build()

    private val idPortenProperties: IdPortenProperties
        get() = IdPortenProperties(
            tokenUrl = tokenUrl,
            clientId = clientId,
            scope = scope,
            configUrl = configUrl,
            virksomhetSertifikatPath = virksomhetssertifikatPath
        )
}

@Profile("mock-alt")
@Configuration
open class IdPortenClientConfigMockAlt(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${idporten_token_url}") private val tokenUrl: String
) {
    @Bean
    open fun idPortenClient(): IdPortenClient {
        val idPortenClient = IdPortenClientMockAlt(idPortenWebClient, tokenUrl)
        return createTimerProxy("IdPortenClient", idPortenClient, IdPortenClient::class.java)
    }

    private val idPortenWebClient: WebClient
        get() = proxiedWebClientBuilder.build()

    private class IdPortenClientMockAlt(
        private val idPortenWebClient: WebClient,
        private val tokenUrl: String
    ) : IdPortenClient {

        override suspend fun requestToken(attempts: Int, headers: HttpHeaders): AccessToken {
            val response = idPortenWebClient.post()
                .uri(tokenUrl)
                .retrieve()
                .awaitBody<IdPortenAccessTokenResponse>()
            return AccessToken(response.accessToken, response.expiresIn)
        }
    }
}
