package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
@Import(KontonummerRessurs::class)
open class KontonummerConfig(
    @Value("\${oppslag_api_baseurl}") private val baseurl: String,
    private val redisService: RedisService
) {

    @Bean
    open fun kontonummerService(kontonummerClient: KontonummerClient): KontonummerService {
        return KontonummerService(kontonummerClient)
    }

    @Bean
    open fun kontonummerClient(): KontonummerClient {
        return KontonummerClientImpl(client, baseurl, redisService)
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
            .register(
                ClientRequestFilter { it.headers.putSingle(HEADER_NAV_APIKEY, System.getenv(OPPSLAGAPI_APIKEY)) }
            )

    companion object {
        private const val OPPSLAGAPI_APIKEY = "OPPSLAGAPI_APIKEY"
    }
}
