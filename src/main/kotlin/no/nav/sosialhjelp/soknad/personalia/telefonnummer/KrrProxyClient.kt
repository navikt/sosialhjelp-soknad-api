package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.KRR_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_NAV_PERSONIDENT
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KrrProxyClient(
    @Value("\${krr_proxy_url}") private val krrProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val redisService: RedisService,
    private val tokendingsService: TokendingsService,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = unproxiedWebClientBuilder(webClientBuilder).build()

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
            webClient.get()
                .uri("${krrProxyUrl}rest/v1/person")
                .header(AUTHORIZATION, BEARER + tokenxToken)
                .header(HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
                .header(HEADER_NAV_PERSONIDENT, ident)
                .retrieve()
                .bodyToMono<DigitalKontaktinformasjon>()
                .block()
                ?.also { lagreTilCache(ident, it) }
        } catch (e: Unauthorized) {
            log.warn("Krr - 401 Unauthorized - ${e.message}")
            null
        } catch (e: Forbidden) {
            log.warn("Krr - 403 Forbidden - ${e.message}")
            null
        } catch (e: NotFound) {
            log.info("Krr - 404 Not Found")
            null
        } catch (e: Exception) {
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
            log.warn("Noe feilet ved lagring av krr-informasjon til redis", e)
        }
    }

    private val tokenxToken: String
        get() = runBlocking {
            tokendingsService.exchangeToken(getUserIdFromToken(), getToken(), fssProxyAudience)
        }

    companion object {
        private val log by logger()
    }
}
