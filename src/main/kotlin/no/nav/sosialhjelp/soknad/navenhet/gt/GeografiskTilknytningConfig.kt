package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.client.pdl.PdlConfig
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class GeografiskTilknytningConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    private val stsClient: StsClient,
    private val redisService: RedisService
) : PdlConfig(baseurl) {

    @Bean
    open fun geografiskTilknytningClient(): GeografiskTilknytningClient {
        return GeografiskTilknytningClient(client, baseurl, stsClient, redisService)
    }

    @Bean
    open fun geografiskTilknytningService(geografiskTilknytningClient: GeografiskTilknytningClient): GeografiskTilknytningService {
        return GeografiskTilknytningService(geografiskTilknytningClient)
    }

    private val client: Client
        get() = RestUtils.createClient().register(pdlApiKeyRequestFilter)
}