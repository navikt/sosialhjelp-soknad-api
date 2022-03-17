package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class KommuneInfoConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    private val maskinportenClient: MaskinportenClient,
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String
) {

    @Bean
    open fun kommuneInfoService(
        kommuneInfoMaskinportenClient: KommuneInfoMaskinportenClient,
        redisService: RedisService
    ): KommuneInfoService {
        return KommuneInfoService(
            kommuneInfoMaskinportenClient,
            redisService
        )
    }

    @Bean
    open fun kommuneInfoMaskinportenClient(): KommuneInfoMaskinportenClient {
        val kommuneInfoMaskinportenClient = KommuneInfoMaskinportenClientImpl(
            kommuneInfoMaskinportenWebClient,
            maskinportenClient,
            integrasjonsidFiks,
            integrasjonpassordFiks
        )
        return createTimerProxy("KommuneInfoMaskinportenClient", kommuneInfoMaskinportenClient, KommuneInfoMaskinportenClient::class.java)
    }

    private val kommuneInfoMaskinportenWebClient: WebClient
        get() = proxiedWebClientBuilder.baseUrl(digisosApiEndpoint).build()
}
