package no.nav.sosialhjelp.soknad.nymodell.controller

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BegrunnelseDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.NySoknadDto
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.SoknadDto
import no.nav.sosialhjelp.soknad.nymodell.service.SoknadService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/soknad", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadController(
    val soknadService: SoknadService
) {
    @PostMapping("/opprettSoknad")
    fun opprettSoknad(): NySoknadDto = soknadService.opprettNySoknad()

    @GetMapping("/{soknadId}")
    fun hentSoknad(@PathVariable("soknadId") soknadId: UUID): SoknadDto = soknadService.hentSoknad(soknadId)
        ?: throw IkkeFunnetException(melding = "Soknad finnes ikke.")

    @GetMapping("/{soknadId}/begrunnelse")
    fun hentBegrunnelse(@PathVariable("soknadId") soknadId: UUID): BegrunnelseDto =
        soknadService.hentBegrunnelse(soknadId)

    @PutMapping("/{soknadId}/begrunnelse")
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody begrunnelseDto: BegrunnelseDto
    ) {
        // TODO valider at teksten inneholder kun lovlige bokstaver

        soknadService.updateBegrunnelse(soknadId, begrunnelseDto)
    }
}
