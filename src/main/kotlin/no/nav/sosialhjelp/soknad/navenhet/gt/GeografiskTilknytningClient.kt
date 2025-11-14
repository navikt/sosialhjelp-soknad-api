package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.app.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
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
    @param:Value("\${pdl_api_url}") private val baseurl: String,
    @param:Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl) {
    fun hentGeografiskTilknytning(personId: String): GeografiskTilknytningDto? =
        runCatching {
            logger.info("Henter Geografisk Tilknytning fra PDL")

            doRequest(personId)
                ?.let { response -> parse<HentGeografiskTilknytningDto>(response) }
                ?.also { pdlResponse -> pdlResponse.checkForPdlApiErrors() }
                ?.data?.hentGeografiskTilknytning
                ?: throw PdlApiException("Noe feilet mot PDL - hentGeografiskTilknytning - response null?")
        }
            .getOrElse { e ->
                when (e) {
                    is PdlApiException -> throw e
                    else -> {
                        logger.error("Kall til PDL feilet (hentGeografiskTilknytning)", e)
                        throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
                    }
                }
            }

    private fun doRequest(personId: String) =
        baseRequest
            .header(HEADER_TEMA, TEMA_KOM)
            .header(AUTHORIZATION, BEARER + tokenXtoken())
            .bodyValue(PdlRequest(HENT_GEOGRAFISK_TILKNYTNING, variables(personId)))
            .retrieve()
            .bodyToMono<String>()
            .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
            .block()

    private fun tokenXtoken() = texasService.exchangeToken(IdentityProvider.TOKENX, target = pdlAudience)

    private fun variables(ident: String): Map<String, Any> = mapOf("ident" to ident)

    companion object {
        private val logger by logger()
    }
}
