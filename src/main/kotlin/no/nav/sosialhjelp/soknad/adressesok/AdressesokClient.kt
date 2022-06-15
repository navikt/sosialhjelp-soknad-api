package no.nav.sosialhjelp.soknad.adressesok

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.client.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.pdl.AdressesokDto
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.ADRESSE_SOK
import no.nav.sosialhjelp.soknad.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
open class AdressesokClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder
) : PdlClient(webClientBuilder, baseurl) {

    open fun getAdressesokResult(variables: Map<String, Any>): AdressesokResultDto? {
        return try {
            val response = baseRequest
                .header(AUTHORIZATION, BEARER + azureAdToken())
                .bodyValue(PdlRequest(ADRESSE_SOK, variables))
                .retrieve()
                .bodyToMono<String>()
                .retryWhen(pdlRetry)
                .block() ?: throw PdlApiException("Noe feilet mot PDL - sokAdresse - response null?")
            val pdlResponse = parse<AdressesokDto>(response)
            pdlResponse.checkForPdlApiErrors()
            pdlResponse.data?.sokAdresse
        } catch (e: PdlApiException) {
            log.warn("PDL - feil oppdaget i response: {}", e.message, e)
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (adresseSok)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun azureAdToken() = runBlocking { azureadService.getSystemToken(pdlScope) }

    companion object {
        private val log = getLogger(AdressesokClient::class.java)
    }
}
