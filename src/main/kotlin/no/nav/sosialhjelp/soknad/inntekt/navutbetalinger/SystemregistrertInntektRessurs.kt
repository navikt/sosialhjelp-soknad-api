package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/inntekt/systemdata", produces = [MediaType.APPLICATION_JSON_VALUE])
class SystemregistrertInntektRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val navYtelseProxy: NavYtelseProxy,
) {
    @GetMapping
    fun hentSystemregistrerteInntekter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SysteminntekterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return navYtelseProxy.getNavYtelse(behandlingsId)
    }
}

data class SysteminntekterFrontend(
    val systeminntekter: List<SysteminntektFrontend>? = null,
    val utbetalingerFraNavFeilet: Boolean? = null,
)

data class SysteminntektFrontend(
    val inntektType: String? = null,
    val utbetalingsdato: String? = null,
    val belop: Double? = null,
)
