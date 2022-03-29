package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class OrganisasjonConfig(
    @Value("\${ereg_proxy_url}") private val baseurl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService
) {

    @Bean
    open fun organisasjonClient(): OrganisasjonClient {
        return OrganisasjonClientImpl(organisasjonClient, baseurl, fssProxyAudience, tokendingsService)
    }

    private val organisasjonClient: Client
        get() = RestUtils.createClient()
}
