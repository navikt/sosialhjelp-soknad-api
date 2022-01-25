package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.NAVUTBETALINGER_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerDto
import org.eclipse.jetty.http.HttpHeader
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client

interface NavUtbetalingerClient {
    fun ping()
    fun getUtbetalingerSiste40Dager(ident: String): NavUtbetalingerDto?
}

class NavUtbetalingerClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val redisService: RedisService
) : NavUtbetalingerClient {

    override fun ping() {
        client
            .target(baseurl + "ping")
            .request()
            .get().use { response ->
                if (response.status != 200) {
                    log.warn("Ping feilet mot Utbetalinger: ${response.statusInfo}")
                }
            }
    }

    override fun getUtbetalingerSiste40Dager(ident: String): NavUtbetalingerDto? {
        hentFraCache(ident)?.let { return it }

        return try {
            val response: NavUtbetalingerDto = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(ServerErrorException::class)
                ) {
                    client.target(baseurl + "utbetalinger")
                        .request()
                        .header(HttpHeader.AUTHORIZATION.name, BEARER + SubjectHandlerUtils.getToken())
                        .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MdcOperations.MDC_CALL_ID))
                        .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                        .get(NavUtbetalingerDto::class.java)
                }
            }
            lagreTilCache(ident, response)
            response
        } catch (e: Exception) {
            log.error("Utbetalinger - Noe uventet feilet", e)
            return null
        }
    }

    private fun hentFraCache(ident: String): NavUtbetalingerDto? {
        return redisService.get(
            NAVUTBETALINGER_CACHE_KEY_PREFIX + ident,
            NavUtbetalingerDto::class.java
        ) as? NavUtbetalingerDto
    }

    private fun lagreTilCache(ident: String, navUtbetalingerDto: NavUtbetalingerDto) {
        try {
            redisService.setex(
                NAVUTBETALINGER_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(navUtbetalingerDto),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av NavUtbetalingerDto til redis", e)
        }
    }

    companion object {
        private val log = getLogger(NavUtbetalingerClientImpl::class.java)
    }
}
