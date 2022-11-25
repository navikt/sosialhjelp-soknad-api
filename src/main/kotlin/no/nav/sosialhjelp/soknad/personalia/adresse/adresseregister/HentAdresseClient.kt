package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.pdl.HentAdresseDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class HentAdresseClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder
) : PdlClient(webClientBuilder, baseurl) {

    fun hentMatrikkelAdresse(matrikkelId: String): MatrikkeladresseDto? {
        return try {
            val response = baseRequest
                .header(HttpHeaders.AUTHORIZATION, BEARER + azureAdToken())
                .bodyValue(PdlRequest(PdlApiQuery.HENT_ADRESSE, variables(matrikkelId)))
                .retrieve()
                .bodyToMono<String>()
                .retryWhen(pdlRetry)
                .block() ?: throw PdlApiException("Noe feilet mot PDL - hentAdresse - response null?")
            val pdlResponse = parse<HentAdresseDto>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data?.hentAdresse?.matrikkelAdresse.also {
                it
                    ?.run { log.info("hentAdresse - hentet matrikkelAdresse fra PDL") }
                    ?: log.info("hentAdresse - matrikkeladresse er null")
            }
        } catch (e: PdlApiException) {
            log.warn("PDL - feil oppdaget i response: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentAdresse)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }

    private fun variables(matrikkelId: String): Map<String, Any> = mapOf("matrikkelId" to matrikkelId)

    companion object {
        private val log by logger()
    }
}
