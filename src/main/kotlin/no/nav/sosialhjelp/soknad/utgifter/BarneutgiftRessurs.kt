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
@RequestMapping("/soknader/{behandlingsId}/utgifter/barneutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BarneutgiftRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val barneutgiftProxy: BarneutgiftProxy,
) {
    @GetMapping
    fun hentBarneutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BarneutgifterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return barneutgiftProxy.getBarneutgifter(behandlingsId)
    }

    @PutMapping
    fun updateBarneutgifter(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody barneutgifterFrontend: BarneutgifterFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        barneutgiftProxy.updateBarneutgifter(behandlingsId, barneutgifterFrontend)
    }

    data class BarneutgifterFrontend(
        val harForsorgerplikt: Boolean = false,
        val bekreftelse: Boolean? = null,
        val fritidsaktiviteter: Boolean = false,
        val barnehage: Boolean = false,
        val sfo: Boolean = false,
        val tannregulering: Boolean = false,
        val annet: Boolean = false,
    )
}
