package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class NavUtbetalingerConfig(
    @Value("\${oppslag_api_baseurl}") private val baseurl: String,
    @Value("\${oppslag_api_audience}") private val oppslagApiAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService
) {

    @Bean
    open fun navUtbetalingerClient(): NavUtbetalingerClient {
        return NavUtbetalingerClientImpl(client, baseurl, redisService, oppslagApiAudience, tokendingsService)
    }

    private val client: Client
        get() = RestUtils.createClient()
}
