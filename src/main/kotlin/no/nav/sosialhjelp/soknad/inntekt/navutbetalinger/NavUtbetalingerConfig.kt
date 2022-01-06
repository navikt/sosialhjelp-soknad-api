package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class NavUtbetalingerConfig(
    @Value("\${oppslag_api_baseurl}") private val baseurl: String,
    private val redisService: RedisService
) {

    @Bean
    open fun navUtbetalingerService(navUtbetalingerClient: NavUtbetalingerClient): NavUtbetalingerService {
        return NavUtbetalingerService(navUtbetalingerClient)
    }

    @Bean
    open fun navUtbetalingerClient(): NavUtbetalingerClient {
        return NavUtbetalingerClientImpl(client, baseurl, redisService)
    }

    @Bean
    open fun navUtbetalingerPing(navUtbetalingerClient: NavUtbetalingerClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "Oppslag - navUtbetalinger", false)
            try {
                navUtbetalingerClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    @Bean
    open fun utbetalingerFraNavSystemdata(
        organisasjonService: OrganisasjonService,
        navUtbetalingerService: NavUtbetalingerService
    ): UtbetalingerFraNavSystemdata {
        return UtbetalingerFraNavSystemdata(organisasjonService, navUtbetalingerService)
    }

    private val client: Client
        get() = RestUtils.createClient()
            .register(
                ClientRequestFilter { it.headers.putSingle(HEADER_NAV_APIKEY, System.getenv(OPPSLAGAPI_APIKEY)) }
            )

    companion object {
        private const val OPPSLAGAPI_APIKEY = "OPPSLAGAPI_APIKEY"
    }
}
