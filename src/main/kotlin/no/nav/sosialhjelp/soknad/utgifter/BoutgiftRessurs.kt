package no.nav.sosialhjelp.soknad.utgifter

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
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
@RequestMapping("/soknader/{behandlingsId}/utgifter/boutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BoutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val boutgifterProxy: BoutgifterProxy,
) {
    @GetMapping
    fun hentBoutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BoutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return boutgifterProxy.getBoutgifter(behandlingsId)
    }

    @PutMapping
    fun updateBoutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody boutgifterFrontend: BoutgifterFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        boutgifterProxy.updateBoutgifter(behandlingsId, boutgifterFrontend)
    }
}

data class BoutgifterFrontend(
    val bekreftelse: Boolean?,
    val husleie: Boolean = false,
    val strom: Boolean = false,
    val kommunalAvgift: Boolean = false,
    val oppvarming: Boolean = false,
    val boliglan: Boolean = false,
    val annet: Boolean = false,
    val skalViseInfoVedBekreftelse: Boolean = false,
)
