package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfiguration
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KommuneInfoService(private val kommuneInfoClient: KommuneInfoClient) {
    @Cacheable(KommuneInfoCacheConfiguration.CACHE_NAME)
    fun hentAlleKommuneInfo(): Map<String, KommuneInfo>? {
        return hentKommuneInfoFraFiks()
            .associateBy { it.kommunenummer }
            .ifEmpty {
                logger.error("hentAlleKommuneInfo - feiler mot Fiks og cache er tom.")
                null
            }
    }

    private fun hentKommuneInfoFraFiks(): List<KommuneInfo> {
        return kommuneInfoClient.getAll()
            .also { logger.info("Hentet kommuneinfo ved bruk av maskinporten-integrasjon mot ks:fiks") }
    }

    companion object {
        private val logger by logger()
    }
}

@Configuration
class KommuneInfoCacheConfiguration : SoknadApiCacheConfiguration {
    override fun getCacheName() = CACHE_NAME

    override fun getConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofDays(EN_DAG))
            .disableCachingNullValues()

    companion object {
        const val CACHE_NAME = "kommuneinfo"
        const val EN_DAG = 1L
    }
}
