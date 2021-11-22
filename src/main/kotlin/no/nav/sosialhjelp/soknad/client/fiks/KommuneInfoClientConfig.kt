package no.nav.sosialhjelp.soknad.client.fiks

import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClientImpl
import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenService
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class KommuneInfoClientConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String
) {

    @Bean
    open fun kommuneInfoService(
        kommuneInfoClient: KommuneInfoClient,
        idPortenService: IdPortenService,
        redisService: RedisService
    ): KommuneInfoService {
        return KommuneInfoService(kommuneInfoClient, idPortenService, redisService)
    }

    @Bean
    open fun kommuneInfoClient(): KommuneInfoClient {
        val kommuneInfoClient = KommuneInfoClientImpl(kommuneInfoWebClient, fiksProperties())
        return createTimerProxy("KommuneInfoClient", kommuneInfoClient, KommuneInfoClient::class.java)
    }

    private val kommuneInfoWebClient: WebClient
        get() = proxiedWebClientBuilder.build()

    private fun fiksProperties(): FiksProperties {
        return FiksProperties(
            digisosApiEndpoint + PATH_KOMMUNEINFO,
            digisosApiEndpoint + PATH_ALLE_KOMMUNEINFO,
            integrasjonsidFiks,
            integrasjonpassordFiks
        )
    }

    companion object {
        const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
        const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
    }
}
