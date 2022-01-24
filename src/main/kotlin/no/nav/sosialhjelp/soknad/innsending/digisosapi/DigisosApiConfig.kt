package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoConfig
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
@Import(
    KommuneInfoConfig::class
)
open class DigisosApiConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String
) {

    @Bean
    open fun digisosApiService(
        digisosApiClient: DigisosApiClient,
        sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
        innsendingService: InnsendingService,
        henvendelseService: HenvendelseService,
        soknadUnderArbeidService: SoknadUnderArbeidService,
        soknadMetricsService: SoknadMetricsService,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository
    ): DigisosApiService {
        return DigisosApiService(
            digisosApiClient,
            sosialhjelpPdfGenerator,
            innsendingService,
            henvendelseService,
            soknadUnderArbeidService,
            soknadMetricsService,
            soknadUnderArbeidRepository
        )
    }

    @Bean
    open fun dokumentlagerClient(fiksWebClient: WebClient): DokumentlagerClient {
        return DokumentlagerClientImpl(fiksWebClient, properties)
    }

    @Bean
    open fun digisosApiClient(
        kommuneInfoService: KommuneInfoService,
        dokumentlagerClient: DokumentlagerClient
    ): DigisosApiClient {
        val digisosApiClient = DigisosApiClientImpl(kommuneInfoService, dokumentlagerClient, properties)
        return MetricsFactory.createTimerProxy("DigisosApi", digisosApiClient, DigisosApiClient::class.java)
    }

    @Bean
    open fun fiksWebClient(proxiedWebClientBuilder: WebClient.Builder, proxiedHttpClient: HttpClient): WebClient =
        proxiedWebClientBuilder
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
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(digisosObjectMapper))
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(digisosObjectMapper))
            }
            .build()

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

    private val properties = DigisosApiProperties(digisosApiEndpoint, integrasjonsidFiks, integrasjonpassordFiks)

    companion object {
        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}

data class DigisosApiProperties(
    val digisosApiEndpoint: String,
    val integrasjonsidFiks: String,
    val integrasjonpassordFiks: String
)
