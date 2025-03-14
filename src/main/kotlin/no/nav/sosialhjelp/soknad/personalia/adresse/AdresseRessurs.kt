package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.AdresseToNyModellProxy
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
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
@RequestMapping("/soknader/{behandlingsId}/personalia/adresser", produces = [MediaType.APPLICATION_JSON_VALUE])
class AdresseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val adresseProxy: AdresseToNyModellProxy,
) {
    @GetMapping
    fun hentAdresser(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): AdresserFrontend {
        // TODO Sjekk logikken mot ny datamodell
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        return adresseProxy.getAdresser(behandlingsId)
    }

    @PutMapping
    fun updateAdresse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody adresserFrontend: AdresserFrontendInput,
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return adresseProxy.updateAdresse(behandlingsId, adresserFrontend)
    }
}
