package no.nav.sosialhjelp.soknad.navenhet.gt

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.BEHANDLINGSNUMMER_SOKNAD
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_BEHANDLINGSNUMMER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_TEMA
import no.nav.sosialhjelp.soknad.app.Constants.TEMA_KOM
import no.nav.sosialhjelp.soknad.app.client.pdl.HentGeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlApiQuery.HENT_GEOGRAFISK_TILKNYTNING
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlClient
import no.nav.sosialhjelp.soknad.app.client.pdl.PdlRequest
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.redis.GEOGRAFISK_TILKNYTNING_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.PDL_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class GeografiskTilknytningClient(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_audience}") private val pdlAudience: String,
    private val tokendingsService: TokendingsService,
    private val redisService: RedisService,
    webClientBuilder: WebClient.Builder,
) : PdlClient(webClientBuilder, baseurl) {

    fun hentGeografiskTilknytning(ident: String): GeografiskTilknytningDto? {
        hentFraCache(ident)?.let {
            // TODO Ekstra logging
            log.info("Henter geografisk tilknytning fra cache: $it")

            return it
        }

        try {
            val response: String =
                baseRequest
                    .header(HEADER_TEMA, TEMA_KOM)
                    .header(AUTHORIZATION, BEARER + tokenXtoken(ident))
                    .header(HEADER_BEHANDLINGSNUMMER, BEHANDLINGSNUMMER_SOKNAD)
                    .bodyValue(PdlRequest(HENT_GEOGRAFISK_TILKNYTNING, variables(ident)))
                    .retrieve()
                    .bodyToMono<String>()
                    .retryWhen(pdlRetry)
                    .block() ?: throw PdlApiException("Noe feilet mot PDL - hentGeografiskTilknytning - response null?")

            val pdlResponse = parse<HentGeografiskTilknytningDto>(response)
            pdlResponse.checkForPdlApiErrors()
            return pdlResponse.data.hentGeografiskTilknytning
                ?.also {
                    // TODO Ekstra logging
                    log.info("Lagrer geografisk tilknytning til cache: $it")

                    lagreTilCache(ident, it)
                }
        } catch (e: PdlApiException) {
            throw e
        } catch (e: Exception) {
            log.error("Kall til PDL feilet (hentGeografiskTilknytning)")
            throw TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e)
        }
    }

    private fun tokenXtoken(ident: String) = runBlocking {
        tokendingsService.exchangeToken(ident, getToken(), pdlAudience)
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
