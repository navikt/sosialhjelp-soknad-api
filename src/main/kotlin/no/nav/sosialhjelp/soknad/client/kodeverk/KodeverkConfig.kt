package no.nav.sosialhjelp.soknad.client.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class KodeverkConfig(
    @Value("\${kodeverk_proxy_url}") private val baseurl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService
) {

    @Bean
    open fun kodeverkService(kodeverkClient: KodeverkClient): KodeverkService {
        return KodeverkService(kodeverkClient, redisService)
    }

    @Bean
    open fun kodeverkClient(): KodeverkClient {
        return KodeverkClientImpl(client, baseurl, redisService, tokendingsService, fssProxyAudience)
    }

    private val client: Client
        get() = RestUtils.createClient().register(kodeverkMapper)

    open val kodeverkMapper: ObjectMapper
        get() = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
}
