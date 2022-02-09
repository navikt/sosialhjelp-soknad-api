package no.nav.sosialhjelp.soknad.navenhet

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS
import no.nav.sosialhjelp.soknad.client.config.RetryUtils.DEFAULT_MAX_ATTEMPTS
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.CACHE_24_HOURS_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.GT_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.GT_LAST_POLL_TIME_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import org.slf4j.LoggerFactory.getLogger
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.ws.rs.NotFoundException
import javax.ws.rs.ServerErrorException
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation

interface NorgClient {
    fun hentNavEnhetForGeografiskTilknytning(geografiskTilknytning: String): NavEnhetDto?
    fun ping()
}

class NorgClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val redisService: RedisService
) : NorgClient {

    override fun hentNavEnhetForGeografiskTilknytning(geografiskTilknytning: String): NavEnhetDto? {
        val request: Invocation.Builder = lagRequest(baseurl + "enhet/navkontor/" + geografiskTilknytning)
        return try {
            val response = runBlocking {
                retry(
                    attempts = DEFAULT_MAX_ATTEMPTS,
                    initialDelay = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(ServerErrorException::class)
                ) {
                    request.get()
                }
            }
            if (response.status != 200) {
                log.warn(
                    "Feil statuskode ved kall mot NORG/gt: {}, respons: {}",
                    response.status,
                    response.readEntity(String::class.java)
                )
                return null
            }
            val rsNorgEnhet = response.readEntity(NavEnhetDto::class.java)
            lagreTilCache(geografiskTilknytning, rsNorgEnhet)
            rsNorgEnhet
        } catch (e: NotFoundException) {
            log.warn("Fant ikke norgenhet for gt $geografiskTilknytning")
            null
        } catch (e: RuntimeException) {
            log.warn("Noe uventet feilet ved kall til NORG/gt", e)
            throw TjenesteUtilgjengeligException("NORG", e)
        }
    }

    override fun ping() {
        /*
         * Erstatt denne metoden med et skikkelig ping-kall. Vi bruker nå et
         * urelatert tjenestekall fordi denne gir raskt svar (og verifiserer
         * at vi når tjenesten).
         */
        lagRequest(baseurl + "kodeverk/EnhetstyperNorg")
            .get().use { response ->
                if (response.status != 200) {
                    throw RuntimeException("Feil statuskode ved kall mot NORG/gt: ${response.status}, respons: ${response.readEntity(String::class.java)}")
                }
            }
    }

    private fun lagRequest(endpoint: String): Invocation.Builder {
        return client.target(endpoint)
            .request()
            .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
            .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
            .header(HEADER_NAV_APIKEY, System.getenv(NORG2_API_V1_APIKEY))
    }

    private fun lagreTilCache(geografiskTilknytning: String, navEnhetDto: NavEnhetDto) {
        try {
            redisService.setex(
                GT_CACHE_KEY_PREFIX + geografiskTilknytning,
                redisObjectMapper.writeValueAsBytes(navEnhetDto),
                CACHE_24_HOURS_IN_SECONDS
            )
            redisService.set(
                GT_LAST_POLL_TIME_PREFIX + geografiskTilknytning,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toByteArray(StandardCharsets.UTF_8)
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe galt skjedde ved oppdatering av kodeverk til Redis", e)
        }
    }

    companion object {
        private val log = getLogger(NorgClientImpl::class.java)

        private const val NORG2_API_V1_APIKEY = "NORG2_API_V1_APIKEY"
    }
}
