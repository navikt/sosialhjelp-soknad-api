package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
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
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

    private val fiksWebClient = webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .doOnConnected { conn -> conn
                        .addHandlerLast(ReadTimeoutHandler(SENDING_TIL_FIKS_TIMEOUT / 1000))
                        .addHandlerLast(WriteTimeoutHandler(SENDING_TIL_FIKS_TIMEOUT / 1000))
                    }
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(Utils.digisosObjectMapper))
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(Utils.digisosObjectMapper))
        }
        .defaultHeader(Constants.HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(Constants.HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .build()

    @Bean
    open fun digisosApiV1Client(): DigisosApiV1Client {
        return DigisosApiV1ClientImpl(
            digisosApiEndpoint,
            kommuneInfoService,
            dokumentlagerClient,
            krypteringService,
            fiksWebClient
        )
    }

    @Bean
    open fun digisosApiV2Client(): DigisosApiV2Client {
        return DigisosApiV2ClientImpl(
            digisosApiEndpoint,
            dokumentlagerClient,
            krypteringService,
            fiksWebClient
        )
    }

    companion object {
        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
