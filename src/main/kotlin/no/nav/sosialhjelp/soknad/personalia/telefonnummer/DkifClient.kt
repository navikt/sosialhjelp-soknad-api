package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.DKIF_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinfoBolk
import org.eclipse.jetty.http.HttpHeader
import org.slf4j.LoggerFactory.getLogger
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation

interface DkifClient {
    fun ping()
    fun hentDigitalKontaktinfo(ident: String): DigitalKontaktinfoBolk?
}

class DkifClientImpl(
    private val client: Client,
    private val baseurl: String,
    private val redisService: RedisService
) : DkifClient {

    override fun ping() {
        val request: Invocation.Builder = client.target(baseurl + "ping").request()
        request.get().use { response ->
            if (response.status != 200) {
                log.warn("Ping feilet mot Dkif: {}", response.status)
            }
        }
    }

    override fun hentDigitalKontaktinfo(ident: String): DigitalKontaktinfoBolk? {
        return hentFraCache(ident) ?: hentFraDkif(ident)
    }

    private fun hentFraCache(ident: String): DigitalKontaktinfoBolk? {
        return redisService.get(
            DKIF_CACHE_KEY_PREFIX + ident,
            DigitalKontaktinfoBolk::class.java
        ) as? DigitalKontaktinfoBolk
    }

    private fun hentFraDkif(ident: String): DigitalKontaktinfoBolk? {
        val request: Invocation.Builder = lagRequest(baseurl + "v1/personer/kontaktinformasjon", ident)
        return try {
            request.get(DigitalKontaktinfoBolk::class.java).also { lagreTilCache(ident, it) }
        } catch (e: NotAuthorizedException) {
            log.warn("Dkif.api - 401 Unauthorized - {}", e.message)
            null
        } catch (e: ForbiddenException) {
            log.warn("Dkif.api - 403 Forbidden - {}", e.message)
            null
        } catch (e: NotFoundException) {
            log.warn("Dkif.api - 404 Not Found - {}", e.message)
            null
        } catch (e: RuntimeException) {
            log.error("Dkif.api - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("Dkif", e)
        }
    }

    private fun lagreTilCache(ident: String, digitalKontaktinfoBolk: DigitalKontaktinfoBolk) {
        try {
            redisService.setex(
                DKIF_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(digitalKontaktinfoBolk),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av digitalKontaktinfoBolk til redis", e)
        }
    }

    private fun lagRequest(endpoint: String, ident: String): Invocation.Builder {
        return client.target(endpoint)
            .request()
            .header(HttpHeader.AUTHORIZATION.name, BEARER + SubjectHandlerUtils.getToken())
            .header(HeaderConstants.HEADER_CALL_ID, MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID))
            .header(HeaderConstants.HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
            .header(HeaderConstants.HEADER_NAV_PERSONIDENTER, ident)
    }

    companion object {
        private val log = getLogger(DkifClientImpl::class.java)
    }
}
