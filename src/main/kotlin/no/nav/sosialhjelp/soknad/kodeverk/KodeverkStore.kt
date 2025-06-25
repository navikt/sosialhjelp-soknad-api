package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfig.Companion.CACHE_NAME
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KodeverkStore(private val client: KodeverkClient) {
    @Cacheable(CACHE_NAME)
    fun hentKodeverk(kodeverksnavn: String): Map<String, String?> = client.hentKodeverk(kodeverksnavn).toMap()

    @CacheEvict(CACHE_NAME, key = "#kodeverksnavn")
    fun hentKodeverkNoCache(kodeverksnavn: String): Map<String, String?> = client.hentKodeverk(kodeverksnavn).toMap()
}

private fun KodeverkDto.toMap(): Map<String, String?> =
    betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRAK_NORSK_BOKMAL)?.term }
        .toMap()

@Configuration
class KodeverkCacheConfig(
    @param:Value("\${digisos.cache.kodeverk.time-to-live}") private val kodeverkTTL: Long,
) : SoknadApiCacheConfig(CACHE_NAME, Duration.ofSeconds(kodeverkTTL)) {
    companion object {
        const val CACHE_NAME: String = "kodeverk"
    }
}
