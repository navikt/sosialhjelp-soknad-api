package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.fiksServiceConnectionProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class MellomlagringConfig(
    @Value("\${digisos_api_baseurl}") digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") integrasjonpassordFiks: String,
    private val krypteringService: KrypteringService,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    @Bean
    fun mellomlagringClient(): MellomlagringClient {
        return MellomlagringClientImpl(
            krypteringService,
            texasService,
            webClient,
        )
    }

    // egen httpClient for opplasting av filer
    private val mellomlagerHttpClient: HttpClient =
        HttpClient.create(fiksServiceConnectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT.toMillis().toInt())
            .responseTimeout(RESPONSE_TIMEOUT)

    private val webClient =
        webClientBuilder.configureWebClientBuilder(mellomlagerHttpClient)
            .baseUrl(digisosApiEndpoint)
            .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
            .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
            .build()

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(15)
        private val RESPONSE_TIMEOUT = Duration.ofSeconds(60)
    }
}
