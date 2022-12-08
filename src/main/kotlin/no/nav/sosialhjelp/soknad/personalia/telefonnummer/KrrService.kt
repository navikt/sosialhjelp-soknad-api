package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import com.fasterxml.jackson.core.JsonProcessingException
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import no.nav.sosialhjelp.soknad.redis.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.redis.KRR_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.springframework.stereotype.Component

@Component
class KrrService(
    private val krrClient: KrrClient,
    private val redisService: RedisService,
    private val unleash: Unleash
) {

    fun getDigitalKontaktinformasjon(ident: String): DigitalKontaktinformasjon? {
        return hentFraCache(ident) ?: hentFraServer(ident)
    }

    private fun hentFraCache(ident: String): DigitalKontaktinformasjon? {
        return redisService.get(
            key = KRR_CACHE_KEY_PREFIX + ident,
            requestedClass = DigitalKontaktinformasjon::class.java
        ) as? DigitalKontaktinformasjon
    }

    private fun hentFraServer(ident: String): DigitalKontaktinformasjon? {
        return krrClient.getDigitalKontaktinformasjon(ident)?.also { lagreTilCache(ident, it) }
    }

    private fun lagreTilCache(ident: String, digitalKontaktinformasjon: DigitalKontaktinformasjon) {
        try {
            redisService.setex(
                key = KRR_CACHE_KEY_PREFIX + ident,
                value = redisObjectMapper.writeValueAsBytes(digitalKontaktinformasjon),
                timeToLiveSeconds = CACHE_30_MINUTES_IN_SECONDS
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av krr-informasjon til redis", e)
        }
    }

    companion object {
        private val log by logger()
    }
}
