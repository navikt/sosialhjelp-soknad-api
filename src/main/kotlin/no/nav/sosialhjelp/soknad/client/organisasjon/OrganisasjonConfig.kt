package no.nav.sosialhjelp.soknad.client.organisasjon

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestContext
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
            val metadata = PingMetadata(baseurl, "Organisasjon", false)
            try {
                organisasjonClient.ping()
                Ping.lyktes(metadata)
            } catch (e: Exception) {
                Ping.feilet(metadata, e)
            }
        }
    }

    private val organisasjonClient: Client
        get() {
            val apiKey = System.getenv(EREGAPI_APIKEY)
            return RestUtils.createClient()
                .register(
                    ClientRequestFilter { requestContext: ClientRequestContext ->
                        requestContext.headers.putSingle(
                            HeaderConstants.HEADER_NAV_APIKEY,
                            apiKey
                        )
                    }
                )
        }

    companion object {
        private const val EREGAPI_APIKEY = "EREGAPI_APIKEY"
    }
}
