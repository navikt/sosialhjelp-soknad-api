package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createNavFssServiceHttpClient
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider.TOKENX
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Periode
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate

interface UtbetalingerFraNavClient {
    suspend fun getUtbetalingerSiste40Dager(): UtbetalDataDto?
}

@Component
class NavUtbetalingerClientImpl(
    @param:Value("\${utbetaldata_api_baseurl}") private val utbetalDataUrl: String,
    @param:Value("\${utbetaldata_audience}") private val utbetalDataAudience: String,
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) : UtbetalingerFraNavClient {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createNavFssServiceHttpClient()).build()

    override suspend fun getUtbetalingerSiste40Dager(): UtbetalDataDto? {
        logger.info("Henter utbetalingsdata fra: $utbetalDataUrl ")

        val request = NavUtbetalingerRequest(currentUserContext().userId, RETTIGHETSHAVER, periode, UTBETALINGSPERIODE)

        return runCatching { doRequest(request) }
            .onSuccess { logger.info("Hentet ${it?.size} utbetalinger fra utbetaldata tjeneste") }
            .onFailure { logger.error("Hente utbetalinger fra Nav feilet", it) }
            .getOrNull()
            ?.let { UtbetalDataDto(it, false) }
    }

    private suspend fun doRequest(request: NavUtbetalingerRequest): List<Utbetaling>? =
        withContext(Dispatchers.IO) {
            webClient.post()
                .uri("$utbetalDataUrl/utbetaldata/api/v2/hent-utbetalingsinformasjon/ekstern")
                .header(HttpHeaders.AUTHORIZATION, BEARER + getTokenX(currentUserContext().userToken))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono<List<Utbetaling>>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .awaitSingleOrNull()
        }

    private suspend fun getTokenX(userToken: String) =
        texasService.exchangeToken(userToken, TOKENX, target = utbetalDataAudience)

    companion object {
        private val logger by logger()
        private const val UTBETALINGSPERIODE = "UTBETALINGSPERIODE"
        private const val RETTIGHETSHAVER = "RETTIGHETSHAVER"
        private val periode = Periode(LocalDate.now().minusDays(40), LocalDate.now())
    }
}

private data class NavUtbetalingerRequest(
    val ident: String,
    val rolle: String,
    val periode: Periode,
    val periodetype: String,
)
