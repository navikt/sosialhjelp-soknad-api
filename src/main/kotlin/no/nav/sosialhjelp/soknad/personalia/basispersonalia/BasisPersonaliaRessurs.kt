package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.BasisPersonaliaFrontend
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.soknad.BasisPersonaliaController
import no.nav.sosialhjelp.soknad.v2.soknad.PersonaliaDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/personalia/basisPersonalia", produces = [MediaType.APPLICATION_JSON_VALUE])
class BasisPersonaliaRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val basisPersonaliaController: BasisPersonaliaController,
) {
    @GetMapping
    fun hentBasisPersonalia(
        @PathVariable("behandlingsId") behandlingsId: String?,
    ): BasisPersonaliaFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return basisPersonaliaController
            .getBasisPersonalia(UUID.fromString(behandlingsId))
            .toBasisPersonaliaFrontend()
    }
}

private fun PersonaliaDto.toBasisPersonaliaFrontend() =
    BasisPersonaliaFrontend(
        navn = NavnFrontend(navn.fornavn, navn.mellomnavn, navn.etternavn),
        fodselsnummer = SubjectHandlerUtils.getUserIdFromToken(),
        statsborgerskap = statsborgerskap,
    )
