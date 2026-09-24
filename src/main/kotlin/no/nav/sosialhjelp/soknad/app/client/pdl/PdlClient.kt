package no.nav.sosialhjelp.soknad.app.client.pdl

import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.core.JacksonException
import tools.jackson.module.kotlin.readValue

abstract class PdlClient(
    webClientBuilder: WebClient.Builder,
    private val baseurl: String,
) {
    private val pdlWebClient: WebClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient())
            .codecs { it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper)) }
            .defaultHeader(HEADER_BEHANDLINGSNUMMER, BEHANDLINGSNUMMER_SOKNAD)
            .build()

    protected val baseRequest: WebClient.RequestBodySpec
        get() =
            pdlWebClient.post()
                .uri(baseurl)
                .accept(MediaType.APPLICATION_JSON)

    protected inline fun <reified T> parse(response: String): T =
        runCatching { soknadJacksonMapper.readValue<T>(response) }
            .getOrElse {
                if (it is JacksonException) it.clearLocation()
                throw it
            }

    companion object {
        // OBS: nummeret må eksistere i behandlingskatalogen
        private const val HEADER_BEHANDLINGSNUMMER = "behandlingsnummer"
        private const val BEHANDLINGSNUMMER_SOKNAD = "D116"
    }
}
