package no.nav.sosialhjelp.soknad.app.client.pdl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.app.Constants.BEHANDLINGSNUMMER_SOKNAD
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_BEHANDLINGSNUMMER
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient

abstract class PdlClient(
    webClientBuilder: WebClient.Builder,
    private val baseurl: String,
) {
    protected val pdlMapper: ObjectMapper =
        jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())

    private val pdlWebClient: WebClient =
        configureWebClientBuilder(webClientBuilder, createNavFssServiceHttpClient())
            .codecs {
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(pdlMapper))
            }
            .defaultHeader(HEADER_BEHANDLINGSNUMMER, BEHANDLINGSNUMMER_SOKNAD)
            .build()

    protected val baseRequest: WebClient.RequestBodySpec
        get() =
            pdlWebClient.post()
                .uri(baseurl)
                .accept(MediaType.APPLICATION_JSON)

    protected inline fun <reified T> parse(response: String): T =
        runCatching { pdlMapper.readValue<T>(response) }
            .getOrElse {
                if (it is MismatchedInputException || it is JsonProcessingException) it.clearLocation()
                throw it
            }
}
