package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.app.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.pdl.HentGeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_GEOGRAFISK_TILKNYTNING
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GeografiskTilknytningClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl) {
    fun hentGeografiskTilknytning(personId: String): GeografiskTilknytningDto? {
        try {
            val response: String =
                baseRequest
                    .header(HEADER_TEMA, TEMA_KOM)
                    .header(AUTHORIZATION, BEARER + tokenXtoken())
                    .bodyValue(PdlRequest(HENT_GEOGRAFISK_TILKNYTNING, variables(personId)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentGeografiskTilknytning - response null?")

            val pdlResponse = parse<HentGeografiskTilknytningDto>(response)
            pdlResponse.checkForPdlApiErrors()
            return pdlResponse.data.hentGeografiskTilknytning
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            logger.error("Kall til PDL feilet (hentGeografiskTilknytning)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun tokenXtoken() = texasService.exchangeToken(IdentityProvider.TOKENX, target = pdlAudience)

    private fun variables(ident: String): Map<String, Any> = mapOf("ident" to ident)

    companion object {
        private val logger by logger()
    }
}
