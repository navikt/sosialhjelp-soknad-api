package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
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
@RequestMapping("/soknader/{behandlingsId}/okonomiskeOpplysninger")
class OkonomiskeOpplysningerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val okonomiskeOpplysningerProxy: OkonomiskeOpplysningerProxy,
) {
    @GetMapping
    fun hentOkonomiskeOpplysninger(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): VedleggFrontends {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        return okonomiskeOpplysningerProxy.getOkonomiskeOpplysninger(behandlingsId)
    }

    @PutMapping
    fun updateOkonomiskOpplysning(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody vedleggFrontend: VedleggFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        okonomiskeOpplysningerProxy.updateOkonomiskeOpplysninger(behandlingsId, vedleggFrontend)
    }
}

data class VedleggFrontends(
    var okonomiskeOpplysninger: List<VedleggFrontend>?,
    // TODO Hvorfor må frontend ha oversikt over slettede vedlegg? Høre med Tore
    var slettedeVedlegg: List<VedleggFrontend>?,
    // TODO Hvorfor trenger frontend et eget flagg for dette? Høre med Tore
    @Schema(description = "True dersom bruker har oppgitt noen økonomiske opplysninger", readOnly = true)
    var isOkonomiskeOpplysningerBekreftet: Boolean,
)
