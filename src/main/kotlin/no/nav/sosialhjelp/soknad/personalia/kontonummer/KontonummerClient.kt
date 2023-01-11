package no.nav.sosialhjelp.soknad.personalia.kontonummer

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontonummerDto
import no.nav.sosialhjelp.soknad.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.KONTONUMMER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

interface KontonummerClient {
    fun getKontonummer(ident: String): KontonummerDto?
}

@Component
class KontonummerClientImpl(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    @Value("\${oppslag_api_audience}") private val oppslagApiAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder,
) : KontonummerClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    private val tokenXtoken: String
        get() = runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), oppslagApiAudience)
        }

    override fun getKontonummer(ident: String): KontonummerDto? {
        hentKontonummerFraCache(ident)?.let { return it }

        return try {
            webClient.get()
                .uri(oppslagApiUrl + "kontonummer")
                .header(AUTHORIZATION, BEARER + tokenXtoken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .retrieve()
                .bodyToMono<KontonummerDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?.also { lagreKontonummerTilCache(ident, it) }
        } catch (e: Unauthorized) {
            log.warn("Kontonummer - 401 Unauthorized - ${e.message}")
            null
        } catch (e: NotFound) {
            log.warn("Kontonummer - 404 Not Found - ${e.message}")
            null
        } catch (e: Exception) {
            log.error("Kontonummer - Noe uventet feilet", e)
            null
        }
    }

    private fun hentKontonummerFraCache(ident: String): KontonummerDto? {
        return redisService.get(KONTONUMMER_CACHE_KEY_PREFIX + ident, KontonummerDto::class.java) as? KontonummerDto
    }

    private fun lagreKontonummerTilCache(ident: String, kontonummerDto: KontonummerDto) {
        try {
            redisService.setex(
                KONTONUMMER_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(kontonummerDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av kontonummerDto til redis", e)
        }
    }

    companion object {
        private val log by logger()
    }
}
