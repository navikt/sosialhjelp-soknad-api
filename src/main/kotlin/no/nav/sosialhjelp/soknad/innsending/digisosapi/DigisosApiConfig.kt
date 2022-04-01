package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DigisosApiConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val serviceUtils: ServiceUtils,
    private val dokumentlagerClient: DokumentlagerClient,
    private val kommuneInfoService: KommuneInfoService
) {

    @Bean
    open fun digisosApiClient(): DigisosApiClient {
        val digisosApiClient = DigisosApiClientImpl(
            digisosApiEndpoint,
            integrasjonsidFiks,
            integrasjonpassordFiks,
            kommuneInfoService,
            dokumentlagerClient,
            serviceUtils
        )
        return MetricsFactory.createTimerProxy("DigisosApi", digisosApiClient, DigisosApiClient::class.java)
    }

    @Bean
    open fun digisosApiPing(digisosApiClient: DigisosApiClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(digisosApiEndpoint, "DigisosApi", true)
            try {
                digisosApiClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
