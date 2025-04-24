package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NorgClient(
    @Value("\${norg_url}") private val norgUrl: String,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    fun hentNavEnhetForGeografiskTilknytning(gt: GeografiskTilknytning): NavEnhetDto? {
        log.info("Henter NavEnhet fra norg for gt: $gt")
        return doHentNavEnhet(gt.value)
    }

    private fun doHentNavEnhet(gt: String): NavEnhetDto? {
        return runCatching {
            webClient.get()
                .uri("$norgUrl/enhet/navkontor/{geografiskTilknytning}", gt)
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                .retrieve()
                .bodyToMono<NavEnhetDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
        }
            .getOrElse { e ->
                when (e) {
                    is NotFound -> {
                        log.warn("Fant ikke norgenhet for gt $gt", e)
                        null
                    }
                    is WebClientResponseException -> {
                        log.warn("Feil statuskode ved kall mot NORG/gt: ${e.statusCode}, respons: ${e.responseBodyAsString}", e)
                        null
                    }
                    else -> {
                        log.warn("Noe uventet feilet ved kall til NORG/gt", e)
                        throw TjenesteUtilgjengeligException("NORG", e)
                    }
                }
            }
    }

    companion object {
        private val log by logger()
    }
}

class TjenesteUtilgjengeligException(message: String, throwable: Throwable?) :
    SosialhjelpSoknadApiException(message, throwable)

data class NavEnhetDto(
    val navn: String,
    val enhetNr: String,
)
