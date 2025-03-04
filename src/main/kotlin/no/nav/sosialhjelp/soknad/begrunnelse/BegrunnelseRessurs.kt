package no.nav.sosialhjelp.soknad.begrunnelse

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
@RequestMapping("/soknader/{behandlingsId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val begrunnelseProxy: BegrunnelseProxy,
) {
    @GetMapping
    fun hentBegrunnelse(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BegrunnelseFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return begrunnelseProxy.getBegrunnelse(behandlingsId)
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody begrunnelseFrontend: BegrunnelseFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        begrunnelseProxy.updateBegrunnelse(behandlingsId, begrunnelseFrontend)
    }

    data class BegrunnelseFrontend(
        val hvaSokesOm: String?,
        val hvorforSoke: String?,
    )
}
