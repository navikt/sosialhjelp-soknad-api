package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
open class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient
) {
    open fun hentGeografiskTilknytning(ident: String): String? {
        val geografiskTilknytningDto = geografiskTilknytningClient.hentGeografiskTilknytning(ident)
        return bydelsnummerEllerKommunenummer(geografiskTilknytningDto)
    }

    private fun bydelsnummerEllerKommunenummer(dto: GeografiskTilknytningDto?): String? {
        if (dto != null && GtType.BYDEL == dto.gtType) {
            return dto.gtBydel
        }
        if (dto != null && GtType.KOMMUNE == dto.gtType) {
            return dto.gtKommune
        }
        log.warn("GeografiskTilknytningDto er ikke av type Bydel eller Kommune -> returnerer null")
        return null
    }

    companion object {
        private val log = getLogger(GeografiskTilknytningService::class.java)
    }
}
