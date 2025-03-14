package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/familie/sivilstatus", produces = [MediaType.APPLICATION_JSON_VALUE])
class SivilstatusRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val sivilstatusProxy: SivilstatusProxy,
) {
    @GetMapping
    fun hentSivilstatus(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SivilstatusFrontend? {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return sivilstatusProxy.getSivilstatus(behandlingsId)
    }

    @PutMapping
    fun updateSivilstatus(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody sivilstatusFrontend: SivilstatusFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        sivilstatusProxy.updateSivilstand(behandlingsId, sivilstatusFrontend)
    }
}
