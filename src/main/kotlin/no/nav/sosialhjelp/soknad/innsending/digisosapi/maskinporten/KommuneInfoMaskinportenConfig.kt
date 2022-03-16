package no.nav.sosialhjelp.soknad.innsending.digisosapi.maskinporten

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class KommuneInfoMaskinportenConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    private val maskinportenClient: MaskinportenClient,
    @Value("\${digisos_api_baseurl}") private val baseurl: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String
) {

    private val kommuneInfoMaskinportenWebClient: WebClient
        get() = proxiedWebClientBuilder.baseUrl(baseurl).build()

    @Bean
    open fun kommuneInfoMaskinportenClient(): KommuneInfoMaskinportenClient {
        val kommuneInfoMaskinportenClient = KommuneInfoMaskinportenClientImpl(
            kommuneInfoMaskinportenWebClient,
            maskinportenClient,
            integrasjonsidFiks,
            integrasjonpassordFiks
        )
        return MetricsFactory.createTimerProxy("Fiks maskinporten kommuneinfo", kommuneInfoMaskinportenClient, KommuneInfoMaskinportenClient::class.java)
    }
}
