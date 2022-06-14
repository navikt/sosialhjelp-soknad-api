package no.nav.sosialhjelp.soknad.client.pdl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.client.config.unproxiedHttpClient
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

abstract class PdlClient(
    webClientBuilder: WebClient.Builder,
    private val baseurl: String,
) {
    private val callId: String? get() = MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID)

    protected val pdlMapper: ObjectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .registerModule(JavaTimeModule())

    private val pdlWebClient: WebClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(pdlMapper))
        }
        .build()

    protected val pdlRetry: RetryBackoffSpec = Retry.backoff(5, Duration.ofMillis(100L)).filter { it is WebClientResponseException }

    open fun ping() {
        pdlWebClient.options()
            .uri(baseurl)
            .header(HEADER_CALL_ID, callId)
            .retrieve()
            .onStatus({ it.value() != 200 }) {
                Mono.error(RuntimeException("PDL - ping feiler: ${it.statusCode()}"))
            }
            .bodyToMono<String>()
            .block()
    }

    protected val baseRequest: WebClient.RequestBodySpec
        get() = pdlWebClient.post()
            .uri(baseurl)
            .accept(MediaType.APPLICATION_JSON)

    protected inline fun <reified T>parse(response: String): T {
        return try {
            pdlMapper.readValue(response)
        } catch (e: MissingKotlinParameterException) {
            e.clearLocation()
            throw e
        } catch (e: JsonProcessingException) {
            e.clearLocation()
            throw e
        }
    }
}
