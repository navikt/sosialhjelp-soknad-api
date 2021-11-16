package no.nav.sosialhjelp.soknad.client.pdl

import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class PdlConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    private val stsClient: StsClient,
    private val redisService: RedisService
) {

    @Bean
    open fun geografiskTilknytningClient(): GeografiskTilknytningClient {
        return GeografiskTilknytningClient(client, baseurl, stsClient, redisService)
    }

    private val client: Client
        get() = RestUtils.createClient()
            .register(
                ClientRequestFilter {
                    it.headers.putSingle(HEADER_NAV_APIKEY, System.getenv(PDLAPI_APIKEY))
                }
            )

    companion object {
        private const val PDLAPI_APIKEY = "PDLAPI_APIKEY"
    }
}
