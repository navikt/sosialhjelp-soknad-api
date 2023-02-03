package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.LoggingUtils.maskerFnr
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.client.config.proxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntekt
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.Sokedata
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class SkatteetatenClient(
    @Value("\${skatteetaten_api_baseurl}") private val baseurl: String,
    private val maskinportenClient: MaskinportenClient,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

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

    fun hentSkattbarinntekt(fnr: String): SkattbarInntekt? {
        val identifikator = if (!MiljoUtils.isNonProduction()) fnr else System.getenv("TESTBRUKER_SKATT") ?: fnr

        val sokedata = Sokedata(
            identifikator = identifikator,
            fom = LocalDate.now().minusMonths(if (LocalDate.now().dayOfMonth > 10) 1 else 2.toLong()),
            tom = LocalDate.now()
        )

        return try {
            skatteetatenWebClient.get()
                .uri("{personidentifikator}/inntekter?fraOgMed={fom}&tilOgMed={tom}", sokedata.identifikator, sokedata.fom.format(formatter), sokedata.tom.format(formatter))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
                .retrieve()
                .bodyToMono<SkattbarInntekt>()
                .onErrorResume(WebClientResponseException.NotFound::class.java) {
                    log.info("Ingen skattbar inntekt funnet")
                    Mono.just(SkattbarInntekt())
                }
                .doOnError { e ->
                    when (e) {
                        is WebClientResponseException -> log.warn("Klarer ikke hente skatteopplysninger ${maskerFnr(e.responseBodyAsString)} status ${e.statusCode}")
                        else -> log.warn("Klarer ikke hente skatteopplysninger - Exception-type: ${e::class.java}")
                    }
                }
                .block()
        } catch (e: Exception) {
            return null
        }
    }

    fun ping() {
        skatteetatenWebClient.options()
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .toBodilessEntity()
            .doOnError { log.warn("SkatteetatenApi - ping feilet") }
            .block()
    }

    companion object {
        private val log by logger()

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
