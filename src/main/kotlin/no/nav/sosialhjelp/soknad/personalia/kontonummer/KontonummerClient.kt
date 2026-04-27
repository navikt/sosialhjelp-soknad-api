package no.nav.sosialhjelp.soknad.personalia.kontonummer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

interface KontonummerClient {
    suspend fun getKontonummer(): KontoDto?
}

@Component
class KontonummerClientImpl(
    @param:Value("\${kontoregister_api_baseurl}") private val kontoregisterUrl: String,
    @param:Value("\${kontoregister_api_audience}") private val kontoregisterAudience: String,
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) : KontonummerClient {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createDefaultHttpClient()).build()

    override suspend fun getKontonummer(): KontoDto? =
        withContext(Dispatchers.IO) {
            runCatching {
                webClient.get()
                    .uri("$kontoregisterUrl/api/borger/v1/hent-aktiv-konto")
                    .header(AUTHORIZATION, BEARER + getTokenX(currentUserContext().userToken))
                    .retrieve()
                    .bodyToMono<KontoDto>()
                    .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                    .awaitSingleOrNull()
            }
                .getOrElse {
                    when (it) {
                        is Unauthorized -> log.warn("Kontoregister konto - 401 Unauthorized - ${it.message}")
                        is NotFound -> log.info("Fant ingen konto i kontoregister - ${it.message}")
                        else -> log.error("Kontoregister konto  - Noe uventet feilet", it)
                    }
                    null
                }
        }

    private suspend fun getTokenX(personId: String) =
        texasService.exchangeToken(personId, IdentityProvider.TOKENX, kontoregisterAudience)

    companion object {
        private val log = getLogger(KontonummerClientImpl::class.java)
    }
}
