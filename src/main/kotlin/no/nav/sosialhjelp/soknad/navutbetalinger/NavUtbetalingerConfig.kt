package no.nav.sosialhjelp.soknad.navutbetalinger

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
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
            val metadata = PingMetadata(baseurl, "Oppslag - navUtbetalinger", false)
            try {
                navUtbetalingerClient.ping()
                Ping.lyktes(metadata)
            } catch (e: Exception) {
                Ping.feilet(metadata, e)
            }
        }
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
