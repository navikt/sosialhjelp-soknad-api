package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.config.KeyRequiredCache
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfig.Companion.CACHE_NAME
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KodeverkStore(private val client: KodeverkClient) {
    @KeyRequiredCache(
        cacheNames = [CACHE_NAME],
        key = "#kodeverksnavn",
    )
    fun hentKodeverk(kodeverksnavn: Kodeverksnavn): Map<String, String?> = client.hentKodeverk(kodeverksnavn).toMap()

    @CacheEvict(CACHE_NAME, key = "#kodeverksnavn")
    fun hentKodeverkNoCache(kodeverksnavn: Kodeverksnavn): Map<String, String?> = client.hentKodeverk(kodeverksnavn).toMap()
}

private fun KodeverkDto.toMap(): Map<String, String?> =
    betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRAK_NORSK_BOKMAL)?.term }
        .toMap()

@Configuration
class KodeverkCacheConfig : SoknadApiCacheConfig(CACHE_NAME, kodeverkTTL) {
    companion object {
        const val CACHE_NAME: String = "kodeverk"
        private val kodeverkTTL: Duration = Duration.ofSeconds(86400L)
    }
}
