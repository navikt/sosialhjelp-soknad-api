package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import org.slf4j.LoggerFactory.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient
) {
    @Cacheable(value = ["PDL-Folkereg-GT"], key = "#ident")
    fun hentGeografiskTilknytning(ident: String): String? {
        val gt = geografiskTilknytningClient.hentGeografiskTilknytning(ident)

        return if (gt == null) {
            log.info("Geografisk tilknytning er null")
            null
        } else when {
            gt.gtType == GtType.BYDEL -> gt.gtBydel ?: error("GtType er BYDEL men bydel er null")
            gt.gtType == GtType.KOMMUNE -> gt.gtKommune ?: error("GtType er KOMMUNE men kommune er null")
            else -> {
                log.warn("gtType er ikke av type Bydel eller Kommune")
                null
            }
        }
    }

    companion object {
        private val log = getLogger(GeografiskTilknytningService::class.java)
    }
}
