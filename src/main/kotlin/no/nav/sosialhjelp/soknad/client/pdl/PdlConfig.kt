package no.nav.sosialhjelp.soknad.client.pdl

import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningClient
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class PdlConfig(
    @Value("\${pdl_api_url}") private val baseurl: String
) {

    @Bean
    open fun pdlPing(geografiskTilknytningClient: GeografiskTilknytningClient): Pingable {
        return Pingable {
            val metadata = Pingable.Ping.PingMetadata(baseurl, "PDL", true)
            try {
                geografiskTilknytningClient.ping()
                Pingable.Ping.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.Ping.feilet(metadata, e)
            }
        }
    }

    companion object {
        private const val PDLAPI_APIKEY = "PDLAPI_APIKEY"

        val pdlApiKeyRequestFilter = ClientRequestFilter { it.headers.putSingle(HEADER_NAV_APIKEY, System.getenv(PDLAPI_APIKEY)) }
    }
}
