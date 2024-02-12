package no.nav.sosialhjelp.soknad.adresseforslag

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionData
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionResult
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

@Component
class AdresseforslagClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder,
    circuitBreakerRegistry: CircuitBreakerRegistry
) : PdlClient(webClientBuilder, baseurl) {

    private fun makeParameters(fritekst: String): AdresseForslagParameters = AdresseForslagParameters(
        completionField = "vegadresse.fritekst",
        maxSuggestions = 10,
        fieldValues = listOf(
            CompletionFieldValue("vegadresse.fritekst", fritekst)
        )
    )

    private val circuitBreaker = circuitBreakerRegistry.circuitBreaker("adresseforslag")

    fun getAdresseforslag(fritekst: String): AdresseCompletionResult? {
        return try {
            val response = circuitBreaker.executeSupplier {
                baseRequest.header(AUTHORIZATION, BEARER + azureAdToken())
                    .bodyValue(TypedPdlRequest(ADRESSE_FORSLAG, mapOf("parameters" to makeParameters(fritekst))))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - sokAdresse - response null?")
            }
            val pdlResponse = parse<PdlResponse<AdresseCompletionData>>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data.forslagAdresse
        } catch (e: PdlApiException) {
            log.warn("PDL - feil oppdaget i response: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (adresseSok)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }

    companion object {
        private val log = getLogger(AdresseforslagClient::class.java)
    }
}
