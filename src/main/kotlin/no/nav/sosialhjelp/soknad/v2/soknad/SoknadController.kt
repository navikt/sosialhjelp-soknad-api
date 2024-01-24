package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Unprotected
@RequestMapping("/soknad", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadController(
    private val soknadService: SoknadService
) {
    @GetMapping("/{soknadId}/hentSoknad")
    fun getSoknad(
        @PathVariable("soknadId") soknadId: UUID,
    ): Soknad {
        return soknadService.findSoknad(soknadId = soknadId)
    }

    @DeleteMapping("/{soknadId}")
    fun deleteSoknad(
        @PathVariable("soknadId") soknadId: UUID,
    ) {
        soknadService.deleteSoknad(soknadId = soknadId)
    }
}
