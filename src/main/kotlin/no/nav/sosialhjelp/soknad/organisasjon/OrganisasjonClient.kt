package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OrganisasjonClient(
    @param:Value("\${ereg_url}") private val eregUrl: String,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient()).build()

    fun hentOrganisasjonNoekkelinfo(orgnummer: String): OrganisasjonNoekkelinfoDto? {
        return runCatching { doGetOrganisasjonNoekkelinfo(orgnummer) }
            .getOrElse {
                when (it) {
                    is NotFound -> {
                        log.warn("Ereg - 404 Not Found - Fant ikke forespurt(e) entitet(er)", it)
                        return null
                    }
                    is BadRequest -> {
                        log.warn("Ereg - 400 Bad Request - Ugyldig(e) parameter(e) i request", it)
                        return null
                    }
                    is ServiceUnavailable -> log.error("Ereg - ${it.statusCode} - Tjenesten er utilgjengelig", it)
                    is InternalServerError -> log.error("Ereg - ${it.statusCode} - Tjenesten er utilgjengelig", it)
                    else -> log.error("Ereg - Noe uventet feilet", it)
                }
                throw TjenesteUtilgjengeligException("EREG", it)
            }
    }

    private fun doGetOrganisasjonNoekkelinfo(orgnummer: String): OrganisasjonNoekkelinfoDto? =
        webClient.get()
            .uri("$eregUrl/v1/organisasjon/{orgnr}/noekkelinfo", orgnummer)
            .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID) ?: "")
            .header(HEADER_CONSUMER_ID, getConsumerId())
            .retrieve()
            .bodyToMono<OrganisasjonNoekkelinfoDto>()
            .block()

    companion object {
        private val log by logger()
    }
}
