package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.navenhet.GeografiskTilknytning
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient,
) {
    fun hentGeografiskTilknytning(personId: String): GeografiskTilknytning? {
        val geografiskTilknytningDto = geografiskTilknytningClient.hentGeografiskTilknytning(personId)
        return bydelsnummerEllerKommunenummer(geografiskTilknytningDto)?.let { GeografiskTilknytning(it) }
    }

    private fun bydelsnummerEllerKommunenummer(dto: GeografiskTilknytningDto?): String? =
        dto?.let {
            when (it.gtType) {
                GtType.BYDEL -> dto.gtBydel
                GtType.KOMMUNE -> dto.gtKommune
                else -> null
            }
        }
            .also { if (it == null) log.warn("GeografiskTilknytningDto er ikke Bydel eller Kommune.") }

    companion object {
        private val log = getLogger(GeografiskTilknytningService::class.java)
    }
}
