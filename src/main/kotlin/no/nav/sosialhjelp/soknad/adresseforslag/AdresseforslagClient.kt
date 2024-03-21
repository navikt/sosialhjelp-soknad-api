package no.nav.sosialhjelp.soknad.adresseforslag

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.adresseforslag.domain.AdresseCompletionResult
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.graphql.client.FieldAccessException
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AdresseforslagClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    webClientBuilder: WebClient.Builder,
    private val azureadService: AzureadService,
) {
    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(baseurl).build()
    internal val graphQlClient = HttpGraphQlClient.builder(webClient).build()

    @CircuitBreaker(name = "adresseforslag")
    fun getAdresseforslag(fritekst: String): Mono<AdresseCompletionResult> =
        graphQlClient.mutate()
            .header(AUTHORIZATION, "Bearer ${azureAdToken()}")
            .build()
            .documentName("forslagAdresse")
            .variable("fritekst", fritekst)
            .retrieve("forslagAdresse")
            .toEntity(AdresseCompletionResult::class.java)
            .doOnError {
                when {
                    it is FieldAccessException -> log.warn("PDL adresseForslag GraphQL-feil: ${it.message}")
                    else -> log.error("PDL adresseForslag feil: ${it.message}")
                }
            }

    companion object {
        private val log = getLogger(AdresseforslagClient::class.java)
    }
}
