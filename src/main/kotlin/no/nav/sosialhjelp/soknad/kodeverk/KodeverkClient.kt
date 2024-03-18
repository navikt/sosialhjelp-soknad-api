package no.nav.sosialhjelp.soknad.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KodeverkClient(
    @Value("\${kodeverk_url}") private val kodeverkUrl: String,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder)
        .codecs {
            it.defaultCodecs().jackson2JsonDecoder(
                Jackson2JsonDecoder(
                    jacksonObjectMapper()
                        .registerModule(JavaTimeModule())
                        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                )
            )
        }
        .baseUrl(kodeverkUrl)
        .build()

    fun hentKodeverk(kodeverksnavn: String): KodeverkDto = runCatching {
        webClient.get()
            .uri { builder ->
                builder.path("/api/v1/kodeverk/{kodeverksnavn}/koder/betydninger")
                    .queryParam("ekskluderUgyldige", "true")
                    .queryParam("spraak", SPRÅK_NORSK_BOKMÅL)
                    .build(kodeverksnavn)
            }
            .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
            .header(HEADER_CONSUMER_ID, getConsumerId())
            .retrieve()
            .bodyToMono<KodeverkDto>()
            .block() ?: error("Kodeverk - ugyldig data")
    }.onFailure { e ->
        if (e is WebClientResponseException) log.warn("Kodeverk - ${e.statusCode}", e)
        else log.error("Kodeverk - noe uventet feilet", e)
    }.getOrThrow()

    companion object {
        private val log = getLogger(KodeverkClient::class.java)

        // Per 2024-03-18 var det ingen verdier for andre språk enn norsk bokmål i kodeverkene vi bruker
        const val SPRÅK_NORSK_BOKMÅL = "nb"
    }
}
