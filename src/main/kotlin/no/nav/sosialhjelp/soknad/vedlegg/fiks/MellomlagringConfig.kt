package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.app.client.config.mdcExchangeFilter
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DokumentlagerClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class MellomlagringConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    private val maskinportenClient: MaskinportenClient,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

    @Bean
    fun mellomlagringClient(): MellomlagringClient {
        return MellomlagringClientImpl(
            dokumentlagerClient,
            krypteringService,
            maskinportenClient,
            webClient
        )
    }

    private val webClient = webClientBuilder
        .baseUrl(digisosApiEndpoint)
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .filter(mdcExchangeFilter)
        .build()

    companion object {
        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
