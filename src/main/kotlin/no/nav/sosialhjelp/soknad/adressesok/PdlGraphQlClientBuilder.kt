package no.nav.sosialhjelp.soknad.adressesok

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

interface PdlGraphQlClientBuilder {
    /**
     * Bygger en HttpGraphQlClient som bruker TokenX-token for autorisasjon.
     * @param ident: Identifikator for bruker som forespørselen gjelder.
     */
    fun buildTokenXClient(ident: String): HttpGraphQlClient

    /**
     * Bygger en HttpGraphQlClient som bruker Azure AD-token for autorisasjon.
     */
    fun buildAzureAdClient(): HttpGraphQlClient
}

@Component
class PdlGraphQlClientBuilderImpl(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val azureadService: AzureadService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) : PdlGraphQlClientBuilder {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(baseurl).build()

    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }
    private fun tokenXtoken(ident: String) = runBlocking { tokendingsService.exchangeToken(ident, SubjectHandlerUtils.getToken(), pdlAudience) }

    internal val graphQlClient = HttpGraphQlClient
        .builder(webClient)
        .header(Constants.HEADER_BEHANDLINGSNUMMER, Constants.BEHANDLINGSNUMMER_SOKNAD)
        .build()

    override fun buildTokenXClient(ident: String): HttpGraphQlClient = graphQlClient.mutate()
        // TODO: Hva er HEADER_TEMA? Dette må avklares før prodsetting.
        .header(Constants.HEADER_TEMA, Constants.TEMA_KOM)
        .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenXtoken(ident)}")
        .build()

    override fun buildAzureAdClient(): HttpGraphQlClient = graphQlClient.mutate()
        .header(HttpHeaders.AUTHORIZATION, "Bearer ${azureAdToken()}")
        .build()
}

/**
 * Creates mock PdlGraphQlClients with a supplied WebClient for easy mocking without authentication.
 */
class PdlGraphQlMockClientBuilder(
    private val mockWebClient: WebClient
) : PdlGraphQlClientBuilder {
    override fun buildTokenXClient(ident: String): HttpGraphQlClient = HttpGraphQlClient.builder(mockWebClient).build()
    override fun buildAzureAdClient(): HttpGraphQlClient = HttpGraphQlClient.builder(mockWebClient).build()
}
