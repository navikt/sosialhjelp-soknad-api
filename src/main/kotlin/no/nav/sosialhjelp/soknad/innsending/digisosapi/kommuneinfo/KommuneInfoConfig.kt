package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
open class KommuneInfoConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val maskinportenClient: MaskinportenClient,
    private val webClientBuilder: WebClient.Builder,
    private val proxiedHttpClient: HttpClient,
) {

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
        get() = webClientBuilder
            .baseUrl(digisosApiEndpoint)
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()
}
