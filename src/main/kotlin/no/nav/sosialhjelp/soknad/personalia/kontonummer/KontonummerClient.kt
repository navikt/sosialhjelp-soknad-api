package no.nav.sosialhjelp.soknad.personalia.kontonummer

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_MAX_ATTEMPTS
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.KONTONUMMER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontonummerDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

interface KontonummerClient {
    fun getKontonummer(ident: String): KontonummerDto?
}

@Component
class KontonummerClientImpl(
    @Value("\${oppslag_api_baseurl}") private val baseurl: String,
    @Value("\${oppslag_api_audience}") private val oppslagApiAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService
) : KontonummerClient {

    private val client: Client = RestUtils.createClient()

    override fun getKontonummer(ident: String): KontonummerDto? {
        hentKontonummerFraCache(ident)?.let { return it }

        return try {
            val response: KontonummerDto = runBlocking {
                retry(
                    attempts = DEFAULT_MAX_ATTEMPTS,
                    initialDelay = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(ServerErrorException::class)
                ) {
                    val tokenXToken = tokendingsService.exchangeToken(
                        SubjectHandlerUtils.getUserIdFromToken(), SubjectHandlerUtils.getToken(), oppslagApiAudience
                    )
                    client.target(baseurl + "kontonummer")
                        .request()
                        .header(AUTHORIZATION, BEARER + tokenXToken)
                        .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
                        .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                        .get(KontonummerDto::class.java)
                }
            }
            lagreKontonummerTilCache(ident, response)
            response
        } catch (e: NotAuthorizedException) {
            log.warn("Kontonummer - 401 Unauthorized - {}", e.message)
            null
        } catch (e: NotFoundException) {
            log.warn("Kontonummer - 404 Not Found - {}", e.message)
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
        private val log = getLogger(KontonummerClientImpl::class.java)
    }
}
