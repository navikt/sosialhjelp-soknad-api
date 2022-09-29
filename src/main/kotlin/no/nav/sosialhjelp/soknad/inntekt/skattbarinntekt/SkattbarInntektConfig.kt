package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sosialhjelp.soknad.app.client.config.proxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
open class SkattbarInntektConfig(
    @Value("\${skatteetaten_api_baseurl}") private val baseurl: String,
    private val maskinportenClient: MaskinportenClient,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

    @Bean
    open fun skatteetatenClient(): SkatteetatenClient {
        return SkatteetatenClientImpl(skatteetatenWebClient, maskinportenClient)
    }

    private val skatteetatenMapper = jacksonObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    private val skatteetatenWebClient: WebClient = proxiedWebClientBuilder(webClientBuilder, proxiedHttpClient)
        .baseUrl(baseurl)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(skatteetatenMapper))
        }
        .build()
}
