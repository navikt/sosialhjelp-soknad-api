package no.nav.sosialhjelp.soknad.organisasjon

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface OrganisasjonClient {
    fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto?
}

@Component
class OrganisasjonClientImpl(
    @Value("\${ereg_proxy_url}") private val eregProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) : OrganisasjonClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(eregProxyUrl).build()

    private val tokenXtoken: String get() = runBlocking {
        tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
    }

    override fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto? {
        return try {
            webClient.get()
                .uri("organisasjon/{orgnr}", orgnr)
                .header(AUTHORIZATION, BEARER + tokenXtoken)
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                .retrieve()
                .bodyToMono<OrganisasjonNoekkelinfoDto>()
                .block()
        } catch (e: WebClientResponseException.NotFound) {
            log.warn("Fss-proxy (ereg) - 404 Not Found - Fant ikke forespurt(e) entitet(er)")
            null
        } catch (e: WebClientResponseException.BadRequest) {
            log.warn("Fss-proxy (ereg) - 400 Bad Request - Ugyldig(e) parameter(e) i request")
            null
        } catch (e: WebClientResponseException.ServiceUnavailable) {
            log.error("Fss-proxy (ereg) - ${e.statusCode} - Tjenesten er utilgjengelig", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        } catch (e: WebClientResponseException.InternalServerError) {
            log.error("Fss-proxy (ereg) - ${e.statusCode} - Tjenesten er utilgjengelig", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        } catch (e: Exception) {
            log.error("Fss-proxy (ereg) - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        }
    }

    companion object {
        private val log = getLogger(OrganisasjonClientImpl::class.java)
    }
}
