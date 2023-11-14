package no.nav.sosialhjelp.soknad.navenhet.gt

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.app.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.app.client.pdl.HentGeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_GEOGRAFISK_TILKNYTNING
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GeografiskTilknytningClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl) {

    fun hentGeografiskTilknytning(ident: String): GeografiskTilknytningDto? {
        try {
            val response: String =
                baseRequest
                    .header(HEADER_TEMA, TEMA_KOM)
                    .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                    .bodyValue(PdlRequest(HENT_GEOGRAFISK_TILKNYTNING, mapOf("ident" to ident)))
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
            log.error("Kall til PDL feilet (hentGeografiskTilknytning)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun tokenXtoken(ident: String) = runBlocking {
        tokendingsService.exchangeToken(ident, getToken(), pdlAudience)
    }

    companion object {
        private val log = getLogger(GeografiskTilknytningClient::class.java)
    }
}
