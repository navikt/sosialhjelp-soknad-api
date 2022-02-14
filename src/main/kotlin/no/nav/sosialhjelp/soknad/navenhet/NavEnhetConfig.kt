package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.feilet
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.lyktes
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(NavEnhetRessurs::class)
open class NavEnhetConfig(
    @Value("\${norg_rest_url}") private val baseurl: String,
    private val redisService: RedisService,
    private val serviceUtils: ServiceUtils
) {

    @Bean
    open fun norgClient(): NorgClient {
        return NorgClientImpl(RestUtils.createClient(), baseurl, redisService)
    }

    @Bean
    open fun navEnhetService(norgClient: NorgClient): NavEnhetService {
        return NavEnhetServiceImpl(norgClient, redisService, serviceUtils)
    }

    @Bean
    open fun norgPing(norgClient: NorgClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "Norg2", false)
            try {
                norgClient.ping()
                lyktes(metadata)
            } catch (e: Exception) {
                feilet(metadata, e)
            }
        }
    }

    @Bean
    open fun finnAdresseService(adressesokService: AdressesokService): FinnAdresseService {
        return FinnAdresseService(adressesokService)
    }
}
