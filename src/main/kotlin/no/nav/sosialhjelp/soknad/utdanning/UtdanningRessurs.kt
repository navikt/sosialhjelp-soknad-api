package no.nav.sosialhjelp.soknad.utdanning

import io.swagger.v3.oas.annotations.media.Schema
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

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/utdanning", produces = [APPLICATION_JSON_VALUE])
class UtdanningRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val utdanningProxy: UtdanningProxy,
) {
    @GetMapping
    fun hentUtdanning(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): UtdanningFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return utdanningProxy.getUtdanning(behandlingsId)
    }

    @PutMapping
    fun updateUtdanning(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody utdanningFrontend: UtdanningFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        utdanningProxy.updateUtdanning(behandlingsId, utdanningFrontend)
    }
}

data class UtdanningFrontend(
    @Schema(nullable = true)
    var erStudent: Boolean?,
    @Schema(nullable = true)
    var studengradErHeltid: Boolean?,
)
