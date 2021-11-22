package no.nav.sosialhjelp.soknad.client.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class KodeverkConfig(
    @Value("\${kodeverk_api_url}") private val baseurl: String,
    private val redisService: RedisService
) {

    @Bean
    open fun kodeverkService(kodeverkClient: KodeverkClient): KodeverkService {
        return KodeverkService(kodeverkClient, redisService)
    }

    @Bean
    open fun kodeverkClient(): KodeverkClient {
        return KodeverkClientImpl(client, baseurl, redisService)
    }

    @Bean
    open fun kodeverkRestPing(kodeverkClient: KodeverkClient): Pingable {
        return Pingable {
            val metadata = Pingable.Ping.PingMetadata(baseurl, "Kodeverk", false)
            try {
                kodeverkClient.ping()
                Pingable.Ping.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.Ping.feilet(metadata, e)
            }
        }
    }

    private val client: Client
        get() {
            val apiKey = System.getenv(KODEVERKAPI_APIKEY)
            return RestUtils.createClient()
                .register(kodeverkMapper)
                .register(
                    ClientRequestFilter { requestContext -> requestContext.headers.putSingle(HEADER_NAV_APIKEY, apiKey) }
                )
        }

    open val kodeverkMapper: ObjectMapper
        get() = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    companion object {
        private const val KODEVERKAPI_APIKEY = "KODEVERKAPI_APIKEY"
    }
}
