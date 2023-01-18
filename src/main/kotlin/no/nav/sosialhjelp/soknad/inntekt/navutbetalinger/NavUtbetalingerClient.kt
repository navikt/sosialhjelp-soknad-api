package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerRequest
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Periode
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.redis.UTBETALDATA_CACHE_KEY_PREFIX
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate

interface NavUtbetalingerClient {
    fun getUtbetalingerSiste40Dager(ident: String): UtbetalDataDto?
}

@Component
class NavUtbetalingerClientImpl(
    @Value("\${utbetaldata_api_baseurl}") private val utbetalDataUrl: String,
    @Value("\${utbetaldata_audience}") private val utbetalDataAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) : NavUtbetalingerClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).filters { it.add(logRequest()) }.build()

    override fun getUtbetalingerSiste40Dager(ident: String): UtbetalDataDto? {
        hentFraCache(ident)?.let { return it }
        log.info("Henter utbetalingsdata fra: $utbetalDataUrl ")

        val periode = Periode(LocalDate.now().minusDays(40), LocalDate.now())
        val request = NavUtbetalingerRequest(ident, RETTIGHETSHAVER, periode, UTBETALINGSPERIODE)

        try {
            val response = webClient.post()
                .uri(utbetalDataUrl + "/utbetaldata/api/v2/hent-utbetalingsinformasjon/ekstern")
                .header(HttpHeaders.AUTHORIZATION, BEARER + tokenXtoken(utbetalDataAudience))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono<List<Utbetaling>>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()

            log.info("Hentet ${response?.size} utbetalinger fra utbetaldata tjeneste")
            val utbetalDataDto = UtbetalDataDto(response, false)
            lagreTilCache(ident, utbetalDataDto)
            return utbetalDataDto
        } catch (e: Exception) {
            log.error("Utbetalinger - Noe uventet feilet", e)
            return null
        }
    }

    private fun tokenXtoken(audience: String): String {
        return runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), audience)
        }
    }

    private fun hentFraCache(ident: String): UtbetalDataDto? {
        return redisService.get(
            UTBETALDATA_CACHE_KEY_PREFIX + ident,
            UtbetalDataDto::class.java
        ) as? UtbetalDataDto
    }

    private fun lagreTilCache(ident: String, utbetalDataDto: UtbetalDataDto) {
        try {
            redisService.setex(
                UTBETALDATA_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(utbetalDataDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av UtbetalDataDto til redis", e)
        }
    }

    private fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
            val sb = StringBuilder("Request: ")
            sb.append("method: ${clientRequest.method()} ")
            sb.append("url: ${clientRequest.url()} ")
            log.info(sb.toString())
            Mono.just(clientRequest)
        }
    }

    companion object {
        private val log by logger()
        private const val UTBETALINGSPERIODE = "UTBETALINGSPERIODE"
        private const val RETTIGHETSHAVER = "RETTIGHETSHAVER"
    }
}
