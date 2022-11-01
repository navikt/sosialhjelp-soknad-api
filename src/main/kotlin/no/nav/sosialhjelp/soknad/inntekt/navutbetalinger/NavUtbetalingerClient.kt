package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerRequest
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Periode
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.NAVUTBETALINGER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.NAVUTBETALINGER_LEGACY_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate

interface NavUtbetalingerClient {
    fun getUtbetalingerSiste40Dager(ident: String): UtbetalDataDto?
    fun getUtbetalingerSiste40DagerLegacy(ident: String): NavUtbetalingerDto?
}

@Component
class NavUtbetalingerClientImpl(
    @Value("\${utbetaldata_api_baseurl}") private val oppslagApiUrl: String,
    @Value("\${utbetaldata_audience}") private val utbetalDataAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) : NavUtbetalingerClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).filters { it.add(logRequest()) }.build()

    private val tokenXtoken: String
        get() = runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), utbetalDataAudience)
        }

    override fun getUtbetalingerSiste40Dager(ident: String): UtbetalDataDto? {
        hentFraCache(ident)?.let { return it }
        log.info("Henter utbetalingsdata fra: $oppslagApiUrl og audience $utbetalDataAudience")

        val periode = Periode(LocalDate.now().minusDays(40), LocalDate.now())
        val request = NavUtbetalingerRequest(ident, RETTIGHETSHAVER, periode, UTBETALINGSPERIODE)

//        TODO fjernes
        log.info("Kaller utbetaldata med body: ${Mono.just(request).block()}")
        try {
            val response = webClient.post()
                .uri(oppslagApiUrl + "/utbetaldata/api/v2/hent-utbetalingsinformasjon/ekstern")
                .header(HttpHeaders.AUTHORIZATION, BEARER + tokenXtoken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .body(Mono.just(request), NavUtbetalingerRequest::class.java)
                .retrieve()
                .bodyToMono<List<Utbetaling>>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()

            log.info("Response fra Utbetaldatatjeneste:  $response")
            val utbetalDataDto = UtbetalDataDto(response, false)
            lagreTilCache(ident, utbetalDataDto)
            return utbetalDataDto
        } catch (e: Exception) {
            log.error("Utbetalinger - Noe uventet feilet", e)
            return null
        }
    }

    override fun getUtbetalingerSiste40DagerLegacy(ident: String): NavUtbetalingerDto? {
        hentFraCacheLegacy(ident)?.let { return it }

        return try {
            webClient.get()
                .uri(oppslagApiUrl + "utbetalinger")
                .header(HttpHeaders.AUTHORIZATION, BEARER + tokenXtoken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .retrieve()
                .bodyToMono<NavUtbetalingerDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?.also { lagreTilCacheLegacy(ident, it) }
        } catch (e: Exception) {
            log.error("Utbetalinger - Noe uventet feilet", e)
            return null
        }
    }

    private fun hentFraCache(ident: String): UtbetalDataDto? {
        return redisService.get(
            NAVUTBETALINGER_CACHE_KEY_PREFIX + ident,
            UtbetalDataDto::class.java
        ) as? UtbetalDataDto
    }

    private fun lagreTilCache(ident: String, utbetalDataDto: UtbetalDataDto) {
        try {
            redisService.setex(
                NAVUTBETALINGER_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(utbetalDataDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av UtbetalDataDto til redis", e)
        }
    }
    private fun hentFraCacheLegacy(ident: String): NavUtbetalingerDto? {
        return redisService.get(
            NAVUTBETALINGER_LEGACY_CACHE_KEY_PREFIX + ident,
            NavUtbetalingerDto::class.java
        ) as? NavUtbetalingerDto
    }

    private fun lagreTilCacheLegacy(ident: String, navUtbetalingerDto: NavUtbetalingerDto) {
        try {
            redisService.setex(
                NAVUTBETALINGER_LEGACY_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(navUtbetalingerDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av NavUtbetalingerDto til redis", e)
        }
    }

    private fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
            val sb = StringBuilder("Request: ")
            // append clientRequest method and url
            sb.append("method: ${clientRequest.method()} ")
            sb.append("url: ${clientRequest.url()} ")
            log.debug(sb.toString())
            Mono.just(clientRequest)
        }
    }

    companion object {
        private val log = getLogger(NavUtbetalingerClientImpl::class.java)
        private const val UTBETALINGSPERIODE = "UTBETALINGSPERIODE"
        private const val RETTIGHETSHAVER = "RETTIGHETSHAVER"
    }
}
