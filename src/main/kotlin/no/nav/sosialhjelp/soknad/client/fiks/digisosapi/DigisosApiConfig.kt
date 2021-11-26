package no.nav.sosialhjelp.soknad.client.fiks.digisosapi

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.soknad.client.fiks.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
open class DigisosApiConfig(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String
) {

    @Bean
    open fun dokumentlagerClient(fiksWebClient: WebClient): DokumentlagerClient {
        return DokumentlagerClientImpl(fiksWebClient, properties)
    }

    @Bean
    open fun digisosApiClient(
        kommuneInfoService: KommuneInfoService,
        dokumentlagerClient: DokumentlagerClient
    ): DigisosApiClient {
        return DigisosApiClientImpl(kommuneInfoService, dokumentlagerClient, properties)
    }

    @Bean
    open fun fiksWebClient(proxiedHttpClient: HttpClient): WebClient =
        WebClient.builder()
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
