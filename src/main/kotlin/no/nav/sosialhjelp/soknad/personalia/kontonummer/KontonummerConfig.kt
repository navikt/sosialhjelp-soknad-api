package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.ws.rs.client.Client

@Configuration
@Import(KontonummerRessurs::class)
open class KontonummerConfig(
    @Value("\${oppslag_api_baseurl}") private val baseurl: String,
    @Value("\${oppslag_api_audience}") private val oppslagApiAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService
) {

    @Bean
    open fun kontonummerService(kontonummerClient: KontonummerClient): KontonummerService {
        return KontonummerService(kontonummerClient)
    }

    @Bean
    open fun kontonummerClient(): KontonummerClient {
        return KontonummerClientImpl(client, baseurl, redisService, oppslagApiAudience, tokendingsService)
    }

    @Bean
    open fun kontonummerPing(kontonummerClient: KontonummerClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "Oppslag - kontonummer", false)
            try {
                kontonummerClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    @Bean
    open fun kontonummerSystemdata(kontonummerService: KontonummerService): KontonummerSystemdata {
        return KontonummerSystemdata(kontonummerService)
    }

    private val client: Client
        get() = RestUtils.createClient()
}
