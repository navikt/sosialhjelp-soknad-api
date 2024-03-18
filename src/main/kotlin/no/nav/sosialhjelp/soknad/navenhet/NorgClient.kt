package no.nav.sosialhjelp.soknad.navenhet

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import no.nav.sosialhjelp.soknad.redis.CACHE_24_HOURS_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.GT_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.GT_LAST_POLL_TIME_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.bodyToMono
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NorgClient(
    @Value("\${norg_url}") private val norgUrl: String,
    private val redisService: RedisService,
    webClientBuilder: WebClient.Builder
) {

    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

    fun hentNavEnhetForGeografiskTilknytning(geografiskTilknytning: String): NavEnhetDto? {
        // TODO Ekstra logging
        log.info("Henter NavEnhet fra norg for gt: $geografiskTilknytning")
        return try {
            webClient.get()
                .uri("$norgUrl/enhet/navkontor/{geografiskTilknytning}", geografiskTilknytning)
                .header(HEADER_CALL_ID, MdcOperations.getFromMDC(MDC_CALL_ID))
                .header(HEADER_CONSUMER_ID, SubjectHandlerUtils.getConsumerId())
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
        private val log by logger()
    }
}
