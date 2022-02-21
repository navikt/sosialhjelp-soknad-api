package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.KRR_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotAuthorizedException
import javax.ws.rs.NotFoundException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

class KrrClient(
    private val client: Client,
    private val krrUrl: String,
    private val krrAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService
) {

    fun ping() {
        client
            .target("$krrUrl/rest/ping")
            .request()
            .options() // burde være GET
            .use {
                if (it.status != 200) {
                    log.warn("Ping feilet mot Krr: {}", it.status)
                }
            }
    }

    fun getDigitalKontaktinformasjon(ident: String): DigitalKontaktinformasjon? {
        return hentFraCache(ident) ?: hentFraServer(ident)
    }

    private fun hentFraCache(ident: String): DigitalKontaktinformasjon? {
        return redisService.get(
            KRR_CACHE_KEY_PREFIX + ident,
            DigitalKontaktinformasjon::class.java
        ) as? DigitalKontaktinformasjon
    }

    private fun hentFraServer(ident: String): DigitalKontaktinformasjon? {
        return try {

            val url = "$krrUrl/rest/v1/person"
            log.info("url: $url")

            client
                .target(url)
                .request()
                .header(AUTHORIZATION, BEARER + tokenxToken(ident))
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_NAV_PERSONIDENT, ident)
                .get(DigitalKontaktinformasjon::class.java)
                .also { lagreTilCache(ident, it) }
        } catch (e: NotAuthorizedException) {
            log.warn("Krr - 401 Unauthorized - {}", e.message)
            null
        } catch (e: ForbiddenException) {
            log.warn("Krr - 403 Forbidden - {}", e.message)
            null
        } catch (e: NotFoundException) {
            log.info("Krr - 404 Not Found")
            null
        } catch (e: RuntimeException) {
            log.error("Krr - Noe uventet feilet", e)
            throw TjenesteUtilgjengeligException("Krr", e)
        }
    }

    private fun lagreTilCache(ident: String, digitalKontaktinformasjon: DigitalKontaktinformasjon) {
        try {
            redisService.setex(
                KRR_CACHE_KEY_PREFIX + ident,
                redisObjectMapper.writeValueAsBytes(digitalKontaktinformasjon),
                CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av digitalKontaktinfoBolk til redis", e)
        }
    }

    private fun tokenxToken(ident: String): String = runBlocking {
        tokendingsService.exchangeToken(ident, getToken(), krrAudience)
    }

    companion object {
        private val log by logger()
    }
}
