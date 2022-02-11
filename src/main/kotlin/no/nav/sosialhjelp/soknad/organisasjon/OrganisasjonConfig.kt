package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class OrganisasjonConfig(
    @Value("\${ereg_proxy_url}") private val baseurl: String
) {

    @Bean
    open fun organisasjonService(organisasjonClient: OrganisasjonClient): OrganisasjonService {
        return OrganisasjonService(organisasjonClient)
    }

    @Bean
    open fun organisasjonClient(): OrganisasjonClient {
        return OrganisasjonClientImpl(organisasjonClient, baseurl)
    }

    private val organisasjonClient: Client
        get() = RestUtils.createClient()
}
