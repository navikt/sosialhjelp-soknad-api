package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KommuneInfoService(private val kommuneInfoClient: KommuneInfoClient) {
    @Cacheable(KommuneInfoCacheConfig.CACHE_NAME, unless = "#result == null || #result.isEmpty()")
    fun hentAlleKommuneInfo(): Map<String, KommuneInfo>? {
        return kommuneInfoClient.getAll()
            .associateBy { it.kommunenummer }
            .ifEmpty {
                logger.error("hentAlleKommuneInfo - feiler mot Fiks og cache er tom.")
                null
            }
    }

    companion object {
        private val logger by logger()
    }
}

@Configuration
class KommuneInfoCacheConfig : SoknadApiCacheConfig(CACHE_NAME, EN_DAG) {
    companion object {
        const val CACHE_NAME = "kommuneinfo"
        private val EN_DAG = Duration.ofDays(1)
    }
}
