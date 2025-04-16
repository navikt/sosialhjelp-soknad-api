package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfiguration
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper.getOrganisasjonsnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NorgService(private val norgClient: NorgClient) {
    @Cacheable(NorgCacheConfiguration.CACHE_NAME, unless = "#result == null")
    fun getEnhetForGt(gt: String): NavEnhet? {
        return runCatching { norgClient.hentNavEnhetForGeografiskTilknytning(GeografiskTilknytning(gt)) }
            .onSuccess { dto -> if (dto != null) logger.info("Bruker NavEnhet fra Norg: $dto") }
            .getOrThrow()
            ?.toNavEnhet(gt)
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
class NorgCacheConfiguration : SoknadApiCacheConfiguration {
    override fun getCacheName() = CACHE_NAME

    override fun getConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(ETT_DOGN))
            .disableCachingNullValues()

    companion object {
        const val CACHE_NAME = "norg"
        private const val ETT_DOGN = 60 * 60L * 24
    }
}

fun NavEnhetDto.toNavEnhet(gt: String): NavEnhet {
    return NavEnhet(
        enhetsnummer = enhetNr,
        enhetsnavn = navn,
        kommunenavn = null,
        orgnummer = getSosialOrgNr(enhetNr, gt),
    )
}

private fun getSosialOrgNr(
    enhetNr: String?,
    gt: String,
): String? {
    return when {
        enhetNr == "0513" && gt == "3434" -> {
            /*
                Jira sak 1200

                Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
                Dette er en midlertidig fix for å få denne casen til å fungere.
             */
            "974592274"
        }
        enhetNr == "0511" && gt == "3432" -> "964949204"
        enhetNr == "1620" && gt == "5014" -> "913071751"
        else -> getOrganisasjonsnummer(enhetNr)
    }
}
