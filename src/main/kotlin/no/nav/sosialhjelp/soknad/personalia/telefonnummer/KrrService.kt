package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import no.nav.sosialhjelp.soknad.valkey.CACHE_30_MINUTES_IN_SECONDS
import no.nav.sosialhjelp.soknad.valkey.KRR_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.valkey.ValkeyService
import no.nav.sosialhjelp.soknad.valkey.ValkeyUtils.valkeyObjectMapper
import org.springframework.stereotype.Component

@Component
class KrrService(
    private val krrClient: KrrClient,
    private val valkeyService: ValkeyService,
) {
    fun getDigitalKontaktinformasjon(ident: String): DigitalKontaktinformasjon? {
        return hentFraCache(ident) ?: hentFraServer(ident)
    }

    private fun hentFraCache(ident: String): DigitalKontaktinformasjon? {
        return valkeyService.get(
            key = KRR_CACHE_KEY_PREFIX + ident,
            requestedClass = DigitalKontaktinformasjon::class.java,
        ) as? DigitalKontaktinformasjon
    }

    private fun hentFraServer(ident: String): DigitalKontaktinformasjon? {
        return krrClient.getDigitalKontaktinformasjon(ident)?.also { lagreTilCache(ident, it) }
    }

    private fun lagreTilCache(
        ident: String,
        digitalKontaktinformasjon: DigitalKontaktinformasjon,
    ) {
        try {
            valkeyService.setex(
                key = KRR_CACHE_KEY_PREFIX + ident,
                value = valkeyObjectMapper.writeValueAsBytes(digitalKontaktinformasjon),
                timeToLiveSeconds = CACHE_30_MINUTES_IN_SECONDS,
            )
        } catch (e: JsonProcessingException) {
            log.warn("Noe feilet ved lagring av krr-informasjon til valkey", e)
        }
    }

    companion object {
        private val log by logger()
    }
}
