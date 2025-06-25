package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KrrClient(
    @param:Value("\${krr_url}") private val krrUrl: String,
    @param:Value("\${krr_audience}") private val krrAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).baseUrl(krrUrl).build()

    fun getDigitalKontaktinformasjon(personId: String): KontaktInfoResponse? =
        runCatching {
            logger.info("Henter Digital kontaktinformasjon fra KRR")
            doPostRequest(personId)
        }
            .getOrElse { e ->
                when (e) {
                    is Unauthorized -> {
                        logger.warn("Krr - 401 Unauthorized - ${e.message}")
                        throw e
                    }
                    is Forbidden -> {
                        logger.warn("Krr - 403 Forbidden - ${e.message}")
                        throw e
                    }
                    is NotFound -> {
                        logger.info("Krr - 404 Not Found")
                        null
                    }
                    else -> throw TjenesteUtilgjengeligException("Krr - Noe uventet feilet", e)
                }
            }

    private fun doPostRequest(personId: String): KontaktInfoResponse? =
        webClient
            .post()
            .uri("/rest/v1/personer")
            .header(AUTHORIZATION, BEARER + tokenxToken)
            .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
            .bodyValue(KontaktInfoRequest(listOf(personId)))
            .retrieve()
            .bodyToMono<KontaktInfoResponse>()
            .block()

    private val tokenxToken: String
        get() = texasService.exchangeToken(IdentityProvider.TOKENX, target = krrAudience)

    companion object {
        private val logger by logger()
    }
}

data class KontaktInfoRequest(
    val personidenter: List<String>,
)

data class KontaktInfoResponse(
    val personer: Map<String, DigitalKontaktinformasjon>?,
    val feil: Map<String, String>?,
)
