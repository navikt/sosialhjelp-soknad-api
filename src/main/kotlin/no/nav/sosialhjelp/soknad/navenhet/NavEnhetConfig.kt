package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningClient
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(NavEnhetRessurs::class)
open class NavEnhetConfig(
    @Value("\${norg_rest_url}") private val baseurl: String,
    private val redisService: RedisService,
    private val geografiskTilknytningClient: GeografiskTilknytningClient
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

    @Bean
    open fun geografiskTilknytningService(): GeografiskTilknytningService {
        return GeografiskTilknytningService(geografiskTilknytningClient)
    }

    @Bean
    open fun finnAdresseService(adressesokService: AdressesokService): FinnAdresseService {
        return FinnAdresseService(adressesokService)
    }
}
