package no.nav.sosialhjelp.soknad.inntekt.formue

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
@RequestMapping("/soknader/{behandlingsId}/inntekt/formue", produces = [MediaType.APPLICATION_JSON_VALUE])
class FormueRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val formueProxy: FormueProxy,
) {
    @GetMapping
    fun hentFormue(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): FormueFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return formueProxy.getFormue(behandlingsId)
    }

    @PutMapping
    fun updateFormue(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody formueFrontend: FormueFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        formueProxy.updateFormue(behandlingsId, formueFrontend)
    }

    data class FormueFrontend(
        val brukskonto: Boolean = false,
        val sparekonto: Boolean = false,
        val bsu: Boolean = false,
        val livsforsikring: Boolean = false,
        val verdipapirer: Boolean = false,
        val annet: Boolean = false,
        val beskrivelseAvAnnet: String?,
    )
}
