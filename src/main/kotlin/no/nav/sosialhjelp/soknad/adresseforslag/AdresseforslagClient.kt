package no.nav.sosialhjelp.soknad.adressesok

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionData
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseForslagParameters
import no.nav.sosialhjelp.soknad.adresseforslag.domain.CompletionFieldValue
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.ADRESSE_FORSLAG
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlResponse
import no.nav.sosialhjelp.soknad.app.client.pdl.TypedPdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class AdresseforslagClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder,
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) : PdlClient(webClientBuilder, baseurl) {

    private fun toVariables(fritekst: String): Map<String, AdresseForslagParameters> = mapOf(
        "parameters" to AdresseForslagParameters(
            completionField = "vegadresse.fritekst",
            maxSuggestions = 10,
            fieldValues = listOf(
                CompletionFieldValue("vegadresse.fritekst", fritekst)
            )
        )
    )

    fun getAdresseforslag(fritekst: String): Mono<PdlResponse<AdresseCompletionData?>> {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker("adresseforslag")

        return Mono.fromCallable {
            baseRequest.header(AUTHORIZATION, BEARER + azureAdToken())
                .bodyValue(TypedPdlRequest(ADRESSE_FORSLAG, toVariables(fritekst)))
                .retrieve()
                .bodyToMono<PdlResponse<AdresseCompletionData?>>()
        }.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .flatMap { it }
            .onErrorResume { e ->
                when (e) {
                    is PdlApiException -> {
                        log.warn("PDL - feil oppdaget i response: ${e.message}", e)
                        Mono.error(e)
                    }

                    else -> {
                        log.error("Kall til PDL feilet (adresseSok)", e)
                        Mono.error(TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e))
                    }
                }
            }
    }

    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }

    companion object {
        private val log = getLogger(AdresseforslagClient::class.java)
    }
}
