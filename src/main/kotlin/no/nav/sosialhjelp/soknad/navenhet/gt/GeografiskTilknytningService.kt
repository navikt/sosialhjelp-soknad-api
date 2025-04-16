package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfiguration
import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient,
    private val personIdService: PersonIdService,
) {
    @Cacheable(GTCacheConfiguration.CACHE_NAME, unless = "#result == null")
    fun hentGeografiskTilknytning(soknadId: UUID): GeografiskTilknytning? {
        return personIdService.findPersonId(soknadId)
            .let { personId -> geografiskTilknytningClient.hentGeografiskTilknytning(personId) }
            .let { dto -> bydelsnummerEllerKommunenummer(dto) }
            ?.let { gt -> GeografiskTilknytning(gt) }
    }

    private fun bydelsnummerEllerKommunenummer(dto: GeografiskTilknytningDto?): String? =
        dto?.let {
            when (it.gtType) {
                GtType.BYDEL -> dto.gtBydel
                GtType.KOMMUNE -> dto.gtKommune
                else -> null
            }
        }
            .also { if (it == null) logger.warn("GeografiskTilknytningDto er ikke Bydel eller Kommune.") }

    companion object {
        private val logger by logger()
    }
}

@Configuration
class GTCacheConfiguration : SoknadApiCacheConfiguration {
    override fun getCacheName() = CACHE_NAME

    override fun getConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(java.time.Duration.ofHours(EN_TIME))
            .disableCachingNullValues()

    companion object {
        const val CACHE_NAME = "geografisk-tilknytning"
        const val EN_TIME = 1L
    }
}
