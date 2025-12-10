package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NorgService(private val norgClient: NorgClient) {
    @Cacheable(NorgCacheConfig.CACHE_NAME, unless = "#result == null")
    fun getEnhetForGt(gt: String): NavEnhet? {
        return runCatching { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
            .onSuccess { dto -> if (dto != null) logger.info("Hentet NavEnhet fra Norg: $dto") }
            .getOrThrow()
            ?.toNavEnhet()
    }

    companion object {
        private val logger by logger()
    }
}

@JvmInline
value class GeografiskTilknytning(val value: String) {
    init {
        if (!value.matches(Regex("^[0-9]+$"))) {
            throw IllegalArgumentException("GT ikke på gyldig format: $value")
        }
    }
}

@Configuration
class NorgCacheConfig : SoknadApiCacheConfig(CACHE_NAME, ETT_DOGN) {
    companion object {
        const val CACHE_NAME = "norg"
        private val ETT_DOGN = Duration.ofDays(1)
    }
}

fun NavEnhetDto.toNavEnhet(): NavEnhet {
    return NavEnhet(
        enhetsnummer = enhetNr,
        enhetsnavn = navn,
        kommunenavn = null,
    )
}
