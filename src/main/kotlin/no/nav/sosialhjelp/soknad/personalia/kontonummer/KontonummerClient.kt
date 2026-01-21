package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

interface KontonummerClient {
    fun getKontonummer(ident: String): KontoDto?
}

@Component
class KontonummerClientImpl(
    @param:Value("\${kontoregister_api_baseurl}") private val kontoregisterUrl: String,
    @param:Value("\${kontoregister_api_audience}") private val kontoregisterAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) : KontonummerClient {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createDefaultHttpClient()).build()

    override fun getKontonummer(ident: String): KontoDto? {
        return try {
            webClient.get()
                .uri("$kontoregisterUrl/api/borger/v1/hent-aktiv-konto")
                .header(AUTHORIZATION, BEARER + tokenX)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID) ?: "")
                .retrieve()
                .bodyToMono<KontoDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
        } catch (e: Unauthorized) {
            log.warn("Kontoregister konto  - 401 Unauthorized - ${e.message}")
            null
        } catch (e: NotFound) {
            log.info("Fant ingen konto i kontoregister - ${e.message}")
            null
        } catch (e: Exception) {
            log.error("Kontoregister konto  - Noe uventet feilet", e)
            null
        }
    }

    private val tokenX get() = texasService.exchangeToken(IdentityProvider.TOKENX, kontoregisterAudience)

    companion object {
        private val log = getLogger(KontonummerClientImpl::class.java)
    }
}
