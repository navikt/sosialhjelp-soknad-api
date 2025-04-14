package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfiguration
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfiguration.Companion.CACHE_NAME
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
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
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }
        .toMap()

@Configuration
class KodeverkCacheConfiguration(
    @Value("\${digisos.cache.kodeverk.time-to-live}") private val kodeverkTTL: Long,
) : SoknadApiCacheConfiguration {
    override fun getCacheName() = CACHE_NAME

    override fun getConfig(): RedisCacheConfiguration {
        return RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(kodeverkTTL))
            .disableCachingNullValues()
    }

    companion object {
        const val CACHE_NAME: String = "kodeverk"
    }
}
