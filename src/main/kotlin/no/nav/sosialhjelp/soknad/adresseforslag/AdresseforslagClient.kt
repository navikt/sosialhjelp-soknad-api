package no.nav.sosialhjelp.soknad.adresseforslag

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.pdl.client.ForslagAdresseGraphQLQuery
import no.nav.sosialhjelp.soknad.pdl.client.ForslagAdresseProjectionRoot
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
    private fun getGraphQlClient() = HttpGraphQlClient.builder(webClient).header(AUTHORIZATION, "Bearer ${azureAdToken()}").build()

    @CircuitBreaker(name = "adresseforslag")
    fun getAdresseforslag(fritekst: String): Mono<AdresseforslagResponse> =
        getGraphQlClient()
            // Flyten her blir litt penere i spring-graphql 1.3.0.
            // se https://github.com/spring-projects/spring-graphql/issues/846
            .document(adresseforslagRequest)
            .variable("fritekst", fritekst)
            .retrieve(adresseforslagQuery.getOperationName())
            .toEntity(AdresseforslagResponse::class.java)
            .doOnError {
                when {
                    it is FieldAccessException -> log.warn("PDL adresseForslag GraphQL-feil: ${it.message}")
                    else -> log.error("PDL adresseForslag feil: ${it.message}")
                }
            }

    companion object {
        private val adresseforslagQuery = ForslagAdresseGraphQLQuery()
        private val adresseforslagRequest = GraphQLQueryRequest(
            adresseforslagQuery,
            ForslagAdresseProjectionRoot<Nothing, Nothing>().suggestions().addressFound()
        ).serialize()

        private val log = getLogger(AdresseforslagClient::class.java)
    }
}
