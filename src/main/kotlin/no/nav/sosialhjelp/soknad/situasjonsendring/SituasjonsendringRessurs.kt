package no.nav.sosialhjelp.soknad.situasjonsendring

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SituasjonsendringFrontend(
    val endring: Boolean? = null,
    val hvaErEndret: String? = null,
)

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/situasjonsendring", produces = [APPLICATION_JSON_VALUE])
class SituasjonsendringRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val situasjonendringProxy: SituasjonendringProxy,
) {
    @GetMapping
    fun hentSituasjonsendring(
        @PathVariable behandlingsId: String,
    ): SituasjonsendringFrontend {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        return situasjonendringProxy.getSituasjonsendring(behandlingsId)
    }

    @PutMapping
    fun updateSituasjonsendring(
        @PathVariable behandlingsId: String,
        @RequestBody situasjonsendring: SituasjonsendringFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return situasjonendringProxy.updateSituasjonsendring(behandlingsId, situasjonsendring)
    }
}
