package no.nav.sosialhjelp.soknad.inntekt.verdi

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
@RequestMapping("/soknader/{behandlingsId}/inntekt/verdier", produces = [MediaType.APPLICATION_JSON_VALUE])
class VerdiRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val verdierProxy: VerdiProxy,
) {
    @GetMapping
    fun hentVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): VerdierFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return verdierProxy.getVerdier(behandlingsId)
    }

    @PutMapping
    fun updateVerdier(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody verdierFrontend: VerdierFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        verdierProxy.updateVerdier(behandlingsId, verdierFrontend)
    }

    data class VerdierFrontend(
        val bekreftelse: Boolean? = null,
        val bolig: Boolean = false,
        val campingvogn: Boolean = false,
        val kjoretoy: Boolean = false,
        val fritidseiendom: Boolean = false,
        val annet: Boolean = false,
        val beskrivelseAvAnnet: String? = null,
    )
}
