package no.nav.sosialhjelp.soknad.navenhet

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.CACHE_24_HOURS_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.GT_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.GT_LAST_POLL_TIME_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.bodyToMono
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface NorgClient {
    fun hentNavEnhetForGeografiskTilknytning(geografiskTilknytning: String): NavEnhetDto?
}

@Component
class NorgClientImpl(
    @Value("\${norg_proxy_url}") private val baseurl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService,
    webClientBuilder: WebClient.Builder
) : NorgClient {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    private val tokenXtoken: String get() = runBlocking {
        tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
    }

    override fun hentNavEnhetForGeografiskTilknytning(geografiskTilknytning: String): NavEnhetDto? {
        return try {
            webClient.get()
                .uri("${baseurl}enhet/navkontor/{geografiskTilknytning}", geografiskTilknytning)
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
                .header(AUTHORIZATION, BEARER + tokenXtoken)
                .retrieve()
                .bodyToMono<NavEnhetDto>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?.also { lagreTilCache(geografiskTilknytning, it) }
        } catch (e: NotFound) {
            log.warn("Fant ikke norgenhet for gt $geografiskTilknytning")
            null
        } catch (e: WebClientResponseException) {
            log.warn("Feil statuskode ved kall mot NORG/gt: ${e.statusCode}, respons: ${e.responseBodyAsString}")
            return null
        } catch (e: Exception) {
            log.warn("Noe uventet feilet ved kall til NORG/gt", e)
            throw TjenesteUtilgjengeligException("NORG", e)
        }
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
    }
}
