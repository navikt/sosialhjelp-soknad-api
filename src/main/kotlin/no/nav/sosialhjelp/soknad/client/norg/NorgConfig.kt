package no.nav.sosialhjelp.soknad.client.norg

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetServiceImpl
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NorgConfig(
    @Value("\${norg_rest_url}") private val baseurl: String,
    private val redisService: RedisService
) {

    @Bean
    open fun norgClient(): NorgClient {
        return NorgClientImpl(RestUtils.createClient(), baseurl, redisService)
    }

    @Bean
    open fun navEnhetService(norgClient: NorgClient): NavEnhetService {
        return NavEnhetServiceImpl(norgClient, redisService)
    }

    @Bean
    open fun norgPing(norgClient: NorgClient): Pingable {
        return Pingable {
            val metadata = Pingable.Ping.PingMetadata(baseurl, "Norg2", false)
            try {
                norgClient.ping()
                Pingable.Ping.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.Ping.feilet(metadata, e)
            }
        }
    }
}
