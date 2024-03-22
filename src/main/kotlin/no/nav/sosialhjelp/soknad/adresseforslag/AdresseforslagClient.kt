package no.nav.sosialhjelp.soknad.adresseforslag

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlinx.coroutines.reactor.mono
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.pdl.ForslagAdresse
import no.nav.sosialhjelp.soknad.pdl.forslagadresse.AdresseCompletionResult
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import no.nav.sosialhjelp.soknad.pdl.ForslagAdresse.Variables as Variables

@Component
class AdresseforslagClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    webClientBuilder: WebClient.Builder,
    private val azureadService: AzureadService,
) {
    internal val graphClient = GraphQLWebClient(url = baseurl, builder = unproxiedWebClientBuilder(webClientBuilder))

    internal fun <T : Any> executeQuery(request: GraphQLClientRequest<T>): Mono<GraphQLClientResponse<T>> =
        mono {
            val token = azureadService.getSystemToken(pdlScope)
            graphClient.execute(request) { header(AUTHORIZATION, "Bearer $token") }
        }

    @CircuitBreaker(name = "adresseforslag")
    fun getAdresseforslag(fritekst: String): Mono<AdresseCompletionResult> =
        executeQuery(ForslagAdresse(Variables(fritekst)))
            .flatMap {
                if (it.errors != null) log.warn("PDL adresseForslag errors: {}", it.errors)
                if (it.data?.forslagAdresse == null) Mono.error(Exception("adresseForslag er null"))
                else Mono.just(it.data!!.forslagAdresse!!)
            }.onErrorResume {
                log.error("PDL adresseForslag feil: {}", it.message)
                Mono.error(it)
            }

    companion object {
        private val log = getLogger(AdresseforslagClient::class.java)
    }
}
