package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class TelefonnummerConfig(
    @Value("\${krr_proxy_url}") private val krrProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
) {

    @Bean
    open fun krrClient(): KrrProxyClient {
        return KrrProxyClient(krrClient, krrProxyUrl, fssProxyAudience, tokendingsService, redisService)
    }

    @Bean
    open fun mobiltelefonService(krrProxyClient: KrrProxyClient): MobiltelefonService {
        return MobiltelefonServiceImpl(krrProxyClient)
    }

    @Bean
    open fun telefonnummerSystemdata(mobiltelefonService: MobiltelefonService): TelefonnummerSystemdata {
        return TelefonnummerSystemdata(mobiltelefonService)
    }

    private val krrClient: Client
        get() = RestUtils.createClient()
}
