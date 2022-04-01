package no.nav.sosialhjelp.soknad.navenhet.gt

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.pdl.HentGeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.client.pdl.PdlApiQuery.HENT_GEOGRAFISK_TILKNYTNING
import no.nav.sosialhjelp.soknad.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.client.redis.GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.PDL_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.common.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.ProcessingException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.client.Client
import javax.ws.rs.core.HttpHeaders.AUTHORIZATION

@Component
class GeografiskTilknytningClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService,
    client: Client = RestUtils.createClient(),
) : PdlClient(client, baseurl) {

    fun hentGeografiskTilknytning(ident: String): GeografiskTilknytningDto? {
        hentFraCache(ident)?.let { return it }

        try {
            val response = runBlocking {
                retry(
                    attempts = RetryUtils.DEFAULT_MAX_ATTEMPTS,
                    initialDelay = RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                    factor = RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                    retryableExceptions = arrayOf(WebApplicationException::class, ProcessingException::class)
                ) {
                    baseRequest
                        .header(HEADER_TEMA, TEMA_KOM)
                        .header(AUTHORIZATION, BEARER + tokendingsService.exchangeToken(ident, getToken(), pdlAudience))
                        .post(requestEntity(HENT_GEOGRAFISK_TILKNYTNING, variables(ident)), String::class.java)
                }
            }

            val pdlResponse = pdlMapper.readValue<HentGeografiskTilknytningDto>(response)

            pdlResponse.checkForPdlApiErrors()

            return pdlResponse.data.hentGeografiskTilknytning
                ?.also { lagreTilCache(ident, it) }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentGeografiskTilknytning)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun hentFraCache(ident: String): GeografiskTilknytningDto? {
        return redisService.get(
            GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX + ident,
            GeografiskTilknytningDto::class.java
        ) as? GeografiskTilknytningDto
    }

    private fun variables(ident: String): Map<String, Any> = mapOf("ident" to ident)

    private fun lagreTilCache(ident: String, geografiskTilknytningDto: GeografiskTilknytningDto) {
        try {
            redisService.setex(
                GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX + ident,
                pdlMapper.writeValueAsBytes(geografiskTilknytningDto),
                PDL_CACHE_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.error(
                "Noe feilet ved serialisering av geografiskTilknytningDto fra Pdl - ${geografiskTilknytningDto.javaClass.name}",
                e
            )
        }
    }

    companion object {
        private val log = getLogger(GeografiskTilknytningClient::class.java)
    }
}
