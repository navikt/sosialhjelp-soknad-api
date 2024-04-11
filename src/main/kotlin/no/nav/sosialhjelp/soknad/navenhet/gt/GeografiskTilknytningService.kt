package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.navenhet.gt.dto.erNorsk
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.toGtStringOrThrow
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class GeografiskTilknytningService(
    private val geografiskTilknytningClient: GeografiskTilknytningClient
) {
    @Cacheable("geografiskTilknytningForIdent")
    fun hentGeografiskTilknytning(ident: String): String? {
        val geografiskTilknytningDto = geografiskTilknytningClient.hentGeografiskTilknytning(ident).block() ?: return null
        if (!geografiskTilknytningDto.erNorsk()) return null
        return geografiskTilknytningDto.toGtStringOrThrow()
    }
}
