package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClientImpl
import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenService
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
        kommuneInfoClient: KommuneInfoClient,
        kommuneInfoMaskinportenClient: KommuneInfoMaskinportenClient,
        idPortenService: IdPortenService,
        redisService: RedisService,
        unleash: Unleash
    ): KommuneInfoService {
        return KommuneInfoService(
            kommuneInfoClient,
            kommuneInfoMaskinportenClient,
            idPortenService,
            redisService,
            unleash
        )
    }

    @Bean
    open fun kommuneInfoClient(): KommuneInfoClient {
        val kommuneInfoClient = KommuneInfoClientImpl(kommuneInfoWebClient, fiksProperties())
        return createTimerProxy("KommuneInfoClient", kommuneInfoClient, KommuneInfoClient::class.java)
    }

    @Bean
    open fun kommuneInfoMaskinportenClient(): KommuneInfoMaskinportenClient {
        val kommuneInfoMaskinportenClient = KommuneInfoMaskinportenClientImpl(
            kommuneInfoWebClient,
            maskinportenClient,
            integrasjonsidFiks,
            integrasjonpassordFiks
        )
        return createTimerProxy("KommuneInfoMaskinportenClient", kommuneInfoMaskinportenClient, KommuneInfoMaskinportenClient::class.java)
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
