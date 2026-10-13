package no.nav.sosialhjelp.soknad.innsending.digisosapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class SendSoknadClientConfig(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
) {
    @Bean(SEND_SOKNAD_WEB_CLIENT)
    fun sendSoknadWebClient(webClientBuilder: WebClient.Builder): WebClient =
        FiksUtils.createFiksWebClient(
            webClientBuilder = webClientBuilder.baseUrl(digisosApiEndpoint),
            integrasjonsidFiks = integrasjonsidFiks,
            integrasjonpassordFiks = integrasjonpassordFiks,
            readTimeout = READ_TIMEOUT,
        )

    companion object {
        const val SEND_SOKNAD_WEB_CLIENT = "sendSoknadWebClient"
        private val READ_TIMEOUT = Duration.ofSeconds(30)
    }
}
