package no.nav.sosialhjelp.soknad.idporten

import no.nav.sosialhjelp.idporten.client.IdPortenClient
import no.nav.sosialhjelp.idporten.client.IdPortenProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class IdPortenClientConfig(
    private val proxiedWebClient: WebClient,
    @Value("\${idporten_token_url}") private val tokenUrl: String,
    @Value("\${idporten_clientid}") private val clientId: String,
    @Value("\${idporten_scope}") private val scope: String,
    @Value("\${idporten_config_url}") private val configUrl: String,
    @Value("\${virksomhetssertifikat_path}") private val virksomhetssertifikatPath: String,
) {

    @Bean
    open fun idPortenClient(): IdPortenClient {
        return IdPortenClient(
            webClient = proxiedWebClient,
            idPortenProperties = idPortenProperties()
        )
    }

    fun idPortenProperties(): IdPortenProperties {
        return IdPortenProperties(
            tokenUrl = tokenUrl,
            clientId = clientId,
            scope = scope,
            configUrl = configUrl,
            virksomhetSertifikatPath = virksomhetssertifikatPath
        )
    }
}