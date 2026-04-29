package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
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
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createDefaultHttpClient())
            .baseUrl(krrUrl)
            .build()

    suspend fun getDigitalKontaktinformasjon(personId: String): KontaktInfoResponse? =
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

    private suspend fun doPostRequest(personId: String): KontaktInfoResponse? =
        withContext(Dispatchers.IO) {
            webClient
                .post()
                .uri("/rest/v1/personer")
                .header(AUTHORIZATION, BEARER + getTokenX(currentUserContext().userToken))
                .bodyValue(KontaktInfoRequest(listOf(personId)))
                .retrieve()
                .bodyToMono<KontaktInfoResponse>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .awaitSingleOrNull()
        }

    private suspend fun getTokenX(userToken: String) = texasService.exchangeToken(userToken, IdentityProvider.TOKENX, target = krrAudience)

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
