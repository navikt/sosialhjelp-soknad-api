package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class OrganisasjonConfig(
    @Value("\${ereg_api_baseurl}") private val baseurl: String
) {

    @Bean
    open fun organisasjonService(organisasjonClient: OrganisasjonClient): OrganisasjonService {
        return OrganisasjonService(organisasjonClient)
    }

    @Bean
    open fun organisasjonClient(): OrganisasjonClient {
        return OrganisasjonClientImpl(organisasjonClient, baseurl)
    }

    @Bean
    open fun organisasjonRestPing(organisasjonClient: OrganisasjonClient): Pingable? {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "Organisasjon", false)
            try {
                organisasjonClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val organisasjonClient: Client
        get() {
            val apiKey = System.getenv(EREGAPI_APIKEY)
            return RestUtils.createClient()
                .register(ClientRequestFilter { it.headers.putSingle(HEADER_NAV_APIKEY, apiKey) })
        }

    companion object {
        private const val EREGAPI_APIKEY = "EREGAPI_APIKEY"
    }
}
