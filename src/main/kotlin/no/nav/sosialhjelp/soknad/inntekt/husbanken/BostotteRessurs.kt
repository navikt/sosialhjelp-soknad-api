package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/inntekt/bostotte", produces = [MediaType.APPLICATION_JSON_VALUE])
class BostotteRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val bostotteProxy: BostotteProxy,
) {
    @GetMapping
    fun hentBostotte(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BostotteFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return bostotteProxy.getBostotte(behandlingsId)
    }

    @PutMapping
    fun updateBostotte(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody bostotteFrontend: BostotteFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        bostotteProxy.updateBostotteBekreftelse(
            soknadId = behandlingsId,
            hasBostotte = bostotteFrontend.bekreftelse,
        )
    }

    @PostMapping("/samtykke")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykke: Boolean,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        bostotteProxy.updateBostotteSamtykke(behandlingsId, samtykke, token)
    }

    data class BostotteFrontend(
        val bekreftelse: Boolean?,
        val samtykke: Boolean?,
        val utbetalinger: List<JsonOkonomiOpplysningUtbetaling>?,
        val saker: List<JsonBostotteSak>?,
        val stotteFraHusbankenFeilet: Boolean?,
        val samtykkeTidspunkt: String?,
    )
}
