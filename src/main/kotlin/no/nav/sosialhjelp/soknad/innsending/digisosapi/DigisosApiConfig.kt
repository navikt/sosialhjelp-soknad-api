package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
open class DigisosApiConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    proxiedWebClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {

    @Bean
    open fun digisosApiV1Client(): DigisosApiV1Client {
        val digisosApiV1Client = DigisosApiV1ClientImpl(
            digisosApiEndpoint,
            integrasjonsidFiks,
            integrasjonpassordFiks,
            kommuneInfoService,
            dokumentlagerClient,
            krypteringService
        )
        return MetricsFactory.createTimerProxy("DigisosApi", digisosApiV1Client, DigisosApiV1Client::class.java)
    }

    @Bean
    open fun digisosApiV2Client(): DigisosApiV2Client {
        val digisosApiV2Client = DigisosApiV2ClientImpl(
            dokumentlagerClient,
            krypteringService,
            webClient
        )
        return MetricsFactory.createTimerProxy("DigisosApiV2", digisosApiV2Client, DigisosApiV2Client::class.java)
    }

    @Bean
    open fun digisosApiPing(digisosApiV1Client: DigisosApiV1Client): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(digisosApiEndpoint, "DigisosApi", true)
            try {
                digisosApiV1Client.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val webClient = proxiedWebClientBuilder
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
        .build()

    companion object {
        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
