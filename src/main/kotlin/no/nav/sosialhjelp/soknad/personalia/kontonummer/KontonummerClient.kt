package no.nav.sosialhjelp.soknad.personalia.kontonummer

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
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontonummerDto
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoregisterRequestDto
import no.nav.sosialhjelp.soknad.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.KONTONUMMER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.KONTOREGISTER_KONTONUMMER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

interface KontonummerClient {
    fun getKontonummer(ident: String): KontoDto?
    fun getKontonummerLegacy(ident: String): KontonummerDto?
}

@Component
class KontonummerClientImpl(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    @Value("\${oppslag_api_audience}") private val oppslagApiAudience: String,
    @Value("\${kontoregister_api_baseurl}") private val kontoregisterUrl: String,
    @Value("\${kontoregister_api_audience}") private val kontoregisterAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder
) : KontonummerClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun getKontonummer(ident: String): KontoDto? {
        hentKontonummerFraCache(ident)?.let { return it }

        return try {
            webClient.post()
                .uri(kontoregisterUrl + "/api/borger/v1/hent-aktiv-konto")
                .header(AUTHORIZATION, BEARER + tokenXtoken(kontoregisterAudience))
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .body(BodyInserters.fromValue(KontoregisterRequestDto(ident)))
                .retrieve()
                .bodyToMono<KontoDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?.also { lagreKontonummerTilCache(ident, it) }
        } catch (e: Unauthorized) {
            log.warn("Kontoregister konto  - 401 Unauthorized - ${e.message}")
            null
        } catch (e: NotFound) {
            log.warn("Kontoregister konto  - 404 Not Found - ${e.message}")
            null
        } catch (e: Exception) {
            log.error("Kontoregister konto  - Noe uventet feilet", e)
            null
        }
    }

    override fun getKontonummerLegacy(ident: String): KontonummerDto? {
        hentKontonummerFraCacheLegacy(ident)?.let { return it }

        return try {
            webClient.get()
                .uri(oppslagApiUrl + "kontonummer")
                .header(AUTHORIZATION, BEARER + tokenXtoken(oppslagApiAudience))
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, getConsumerId())
                .retrieve()
                .bodyToMono<KontonummerDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?.also { lagreKontonummerTilCacheLegacy(ident, it) }
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

    private fun tokenXtoken(audience: String): String {
        return runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), audience)
        }
    }

    private fun hentKontonummerFraCache(ident: String): KontoDto? {
        return redisService.get(KONTOREGISTER_KONTONUMMER_CACHE_KEY_PREFIX + ident, KontoDto::class.java) as? KontoDto
    }

    private fun lagreKontonummerTilCache(ident: String, kontoDto: KontoDto) {
        try {
            redisService.setex(
                KONTOREGISTER_KONTONUMMER_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(kontoDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av kontoDto til redis", e)
        }
    }

    private fun hentKontonummerFraCacheLegacy(ident: String): KontonummerDto? {
        return redisService.get(KONTONUMMER_CACHE_KEY_PREFIX + ident, KontonummerDto::class.java) as? KontonummerDto
    }

    private fun lagreKontonummerTilCacheLegacy(ident: String, kontonummerDto: KontonummerDto) {
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
        private val log = getLogger(KontonummerClientImpl::class.java)
    }
}
