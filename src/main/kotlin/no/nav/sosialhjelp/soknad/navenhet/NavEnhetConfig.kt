package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(NavEnhetRessurs::class)
open class NavEnhetConfig(
    @Value("\${norg_proxy_url}") private val baseurl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService
) {

    @Bean
    open fun norgClient(): NorgClient {
        return NorgClientImpl(RestUtils.createClient(), baseurl, redisService, tokendingsService, fssProxyAudience)
    }

    @Bean
    open fun navEnhetService(norgClient: NorgClient): NavEnhetService {
        return NavEnhetServiceImpl(norgClient, redisService)
    }

    @Bean
    open fun finnAdresseService(adressesokService: AdressesokService): FinnAdresseService {
        return FinnAdresseService(adressesokService)
    }
}
