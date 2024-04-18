package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.oppsummering.dto.Oppsummering
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/oppsummering", produces = [MediaType.APPLICATION_JSON_VALUE])
class OppsummeringRessurs(
    private val oppsummeringService: OppsummeringService,
    private val tilgangskontroll: Tilgangskontroll,
) {
    @GetMapping
    fun getOppsummering(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): Oppsummering {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        return oppsummeringService.hentOppsummering(eier, behandlingsId)
    }
}
