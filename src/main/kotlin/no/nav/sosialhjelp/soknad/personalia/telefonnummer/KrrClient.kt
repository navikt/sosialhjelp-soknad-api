package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KrrClient(
    @Value("\${krr_url}") private val krrUrl: String,
    @Value("\${krr_audience}") private val krrAudience: String,
    @Value("\${krr_scope}") private val krrScope: String,
    private val tokendingsService: TokendingsService,
    private val azureadService: AzureadService,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(krrUrl).build()

    private val tokenxToken: String
        get() = runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), krrAudience)
        }

    fun getDigitalKontaktinformasjon(ident: String): DigitalKontaktinformasjon? {
        return try {
            webClient.get()
                .uri("/rest/v1/person")
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_NAV_PERSONIDENT, ident)
                .retrieve()
                .bodyToMono<DigitalKontaktinformasjon>()
                .block()
        } catch (e: WebClientResponseException.Unauthorized) {
            log.warn("Krr - 401 Unauthorized - ${e.message}")
            null
        } catch (e: WebClientResponseException.Forbidden) {
            log.warn("Krr - 403 Forbidden - ${e.message}")
            null
        } catch (e: WebClientResponseException.NotFound) {
            log.info("Krr - 404 Not Found")
            null
        } catch (e: Exception) {
            log.error("Krr - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("Krr", e)
        }
    }

    fun ping() {
        webClient.get()
            .uri("/rest/ping")
            .accept(APPLICATION_JSON)
            .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
            .header(AUTHORIZATION, BEARER + azureAdToken)
            .retrieve()
            .bodyToMono<Any>()
            .block()
    }

    private val azureAdToken get() = runBlocking { azureadService.getSystemToken(krrScope) }

    companion object {
        private val log by logger()
    }
}
