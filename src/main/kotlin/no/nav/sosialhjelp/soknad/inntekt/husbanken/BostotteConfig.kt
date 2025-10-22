package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createExternalServiceHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class BostotteConfig(
    @param:Value("\${soknad.bostotte.url}") private val bostotteBaseUrl: String,
    webClientBuilder: WebClient.Builder,
) {
    @Bean
    fun husbankenClient(): HusbankenClient = HusbankenClient(husbankenWebClient)

    private val husbankenWebClient: WebClient =
        configureWebClientBuilder(
            webClientBuilder = webClientBuilder,
            httpClient = createExternalServiceHttpClient().responseTimeout(Duration.ofSeconds(10L)),
        )
            .baseUrl(bostotteBaseUrl)
            .build()
}
