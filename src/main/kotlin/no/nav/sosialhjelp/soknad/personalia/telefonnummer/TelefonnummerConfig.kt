package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
@Import(TelefonnummerRessurs::class)
open class TelefonnummerConfig(
    @Value("\${dkif_api_baseurl}") private val baseurl: String,
    private val redisService: RedisService
) {

    @Bean
    open fun dkifClient(): DkifClient {
        return DkifClientImpl(client, baseurl, redisService)
    }

    @Bean
    open fun dkifRestPing(dkifClient: DkifClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "Dkif", false)
            try {
                dkifClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    @Bean
    open fun mobiltelefonService(dkifClient: DkifClient): MobiltelefonService {
        return MobiltelefonServiceImpl(dkifClient)
    }

    @Bean
    open fun telefonnummerSystemdata(mobiltelefonService: MobiltelefonService): TelefonnummerSystemdata {
        return TelefonnummerSystemdata(mobiltelefonService)
    }

    private val client: Client
        get() {
            val apiKey = System.getenv(DKIFAPI_APIKEY)
            return RestUtils.createClient()
                .register(ClientRequestFilter { it.headers.putSingle(HeaderConstants.HEADER_NAV_APIKEY, apiKey) })
        }

    companion object {
        private const val DKIFAPI_APIKEY = "DKIFAPI_APIKEY"
    }
}
