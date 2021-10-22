package no.nav.sosialhjelp.soknad.client.skatteetaten

import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumerImpl.Sokedata
import no.nav.sosialhjelp.soknad.consumer.skatt.dto.SkattbarInntekt
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.maskerFnr
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface SkatteetatenClient {
    fun hentSkattbarinntekt(fnr: String): SkattbarInntekt?
    fun ping()
}

class SkatteetatenClientImpl(
    private val webClient: WebClient,
    private val maskinportenClient: MaskinportenClient
) : SkatteetatenClient {

    override fun hentSkattbarinntekt(fnr: String): SkattbarInntekt? {
        val sokedata = Sokedata()
            .withFom(LocalDate.now().minusMonths(if (LocalDate.now().dayOfMonth > 10) 1 else 2.toLong()))
            .withTom(LocalDate.now())
            .withIdentifikator(System.getenv("TESTBRUKER_SKATT") ?: fnr)

        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("{personidentifikator}/inntekter")
                    .queryParam("fraOgMed", sokedata.fom.format(formatter))
                    .queryParam("tilOgMed", sokedata.tom.format(formatter))
                    .build(sokedata.identifikator)
            }
            .accept(MediaType.APPLICATION_JSON)
            .headers { it.add(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getTokenString()) }
            .retrieve()
            .bodyToMono<SkattbarInntekt>()
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                log.info("Ingen skattbar inntekt funnet")
                Mono.just(SkattbarInntekt())
            }
            .onErrorMap(WebClientResponseException::class.java) { e ->
                val feilmeldingUtenFnr = maskerFnr(e.responseBodyAsString)
                log.warn("Klarer ikke hente skatteopplysninger {} status {} ", feilmeldingUtenFnr, e.statusCode)
                throw e
            }
            .onErrorMap { e ->
                log.warn("Klarer ikke hente skatteopplysninger", e)
                throw e
            }
            .block()
    }

    override fun ping() {
        webClient.options()
            .headers { it.add(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getTokenString()) }
            .retrieve()
            .toBodilessEntity()
            .doOnError { log.warn("SkatteetatenApi - ping feilet") }
            .block()
    }

    companion object {
        private val log = getLogger(SkatteetatenClientImpl::class.java)

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
