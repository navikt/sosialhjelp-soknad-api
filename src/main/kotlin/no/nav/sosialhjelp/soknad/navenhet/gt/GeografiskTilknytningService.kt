package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient,
    private val personIdService: PersonIdService,
) {
    // TODO Hvis personen har byttet folkeregistrert adresse - vil cache'n kunne føre til at man får feil GT ?
    @Cacheable(GTCacheConfig.CACHE_NAME, unless = "#result == null")
    fun hentGeografiskTilknytning(soknadId: UUID): String? =
        personIdService.findPersonId(soknadId)
            .let { personId -> geografiskTilknytningClient.hentGeografiskTilknytning(personId) }
            .let { dto -> bydelsnummerEllerKommunenummer(dto) }

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
class GTCacheConfig : SoknadApiCacheConfig(CACHE_NAME, EN_TIME) {
    companion object {
        const val CACHE_NAME = "geografisk-tilknytning"
        private val EN_TIME = Duration.ofHours(1)
    }
}
