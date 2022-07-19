package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface OrganisasjonClient {
    fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto?
}

@Component
class OrganisasjonClientImpl(
    @Value("\${ereg_url}") private val eregUrl: String,
    webClientBuilder: WebClient.Builder,
) : OrganisasjonClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun hentOrganisasjonNoekkelinfo(orgnr: String): OrganisasjonNoekkelinfoDto? {
        return try {
            webClient.get()
                .uri("$eregUrl/v1/organisasjon/{orgnr}/noekkelinfo", orgnr)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .retrieve()
                .bodyToMono<OrganisasjonNoekkelinfoDto>()
                .block()
        } catch (e: WebClientResponseException.NotFound) {
            log.warn("Ereg - 404 Not Found - Fant ikke forespurt(e) entitet(er)")
            null
        } catch (e: WebClientResponseException.BadRequest) {
            log.warn("Ereg - 400 Bad Request - Ugyldig(e) parameter(e) i request")
            null
        } catch (e: WebClientResponseException.ServiceUnavailable) {
            log.error("Ereg - ${e.statusCode} - Tjenesten er utilgjengelig", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        } catch (e: WebClientResponseException.InternalServerError) {
            log.error("Ereg - ${e.statusCode} - Tjenesten er utilgjengelig", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        } catch (e: Exception) {
            log.error("Ereg - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("EREG", e)
        }
    }

    companion object {
        private val log = getLogger(OrganisasjonClientImpl::class.java)
    }
}
