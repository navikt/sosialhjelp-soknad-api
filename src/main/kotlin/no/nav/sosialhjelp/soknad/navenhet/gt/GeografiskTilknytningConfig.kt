package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class GeografiskTilknytningConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService
) {

    @Bean
    open fun geografiskTilknytningClient(): GeografiskTilknytningClient {
        return GeografiskTilknytningClient(client, baseurl, pdlAudience, tokendingsService, redisService)
    }

    @Bean
    open fun pdlPing(geografiskTilknytningClient: GeografiskTilknytningClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "PDL", true)
            try {
                geografiskTilknytningClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val client: Client
        get() = RestUtils.createClient()
}
