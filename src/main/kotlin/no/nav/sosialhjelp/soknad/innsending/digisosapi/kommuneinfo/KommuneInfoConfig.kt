package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

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
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {

    @Bean
    open fun kommuneInfoMaskinportenClient(): KommuneInfoMaskinportenClient {
        return KommuneInfoMaskinportenClientImpl(
            kommuneInfoMaskinportenWebClient,
            maskinportenClient,
            integrasjonsidFiks,
            integrasjonpassordFiks
        )
    }

    private val kommuneInfoMaskinportenWebClient: WebClient =
        webClientBuilder
            .baseUrl(digisosApiEndpoint)
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()
}
