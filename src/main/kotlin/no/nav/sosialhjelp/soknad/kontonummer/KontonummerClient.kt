package no.nav.sosialhjelp.soknad.kontonummer

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KONTONUMMER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService
import no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.objectMapper
import no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.kontonummer.dto.KontonummerDto
import org.eclipse.jetty.http.HttpHeader
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client

interface KontonummerClient {
    fun ping()
    fun getKontonummer(ident: String): KontonummerDto?
}

class KontonummerClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val redisService: RedisService
) : KontonummerClient {

    override fun ping() {
        client
            .target(baseurl + "ping")
            .request()
            .get().use { response ->
                if (response.status != 200) {
                    log.warn("Ping feilet mot Kontonummer: ${response.statusInfo}")
                }
            }
    }

    override fun getKontonummer(ident: String): KontonummerDto? {
        hentKontonummerFraCache(ident)?.let { return it }

        return try {
            val response: KontonummerDto = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(ServerErrorException::class)
                ) {
                    client.target(baseurl + "kontonummer")
                        .request()
                        .header(HttpHeader.AUTHORIZATION.name, HeaderConstants.BEARER + SubjectHandler.getToken())
                        .header(HeaderConstants.HEADER_CALL_ID, MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID))
                        .header(HeaderConstants.HEADER_CONSUMER_ID, SubjectHandler.getConsumerId())
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
                objectMapper.writeValueAsBytes(kontonummerDto),
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
