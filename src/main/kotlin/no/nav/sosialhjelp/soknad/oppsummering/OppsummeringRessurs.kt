package no.nav.sosialhjelp.soknad.oppsummering
import no.nav.sosialhjelp.soknad.oppsummering.dto.Oppsummering
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/soknader/{soknadId}/oppsummering", produces = [MediaType.APPLICATION_JSON_VALUE])
class OppsummeringRessurs(private val oppsummeringService: OppsummeringService) {
    @GetMapping
    fun getOppsummering(
        @PathVariable("soknadId") soknadId: UUID,
    ): Oppsummering = oppsummeringService.hentOppsummering(soknadId)
}
