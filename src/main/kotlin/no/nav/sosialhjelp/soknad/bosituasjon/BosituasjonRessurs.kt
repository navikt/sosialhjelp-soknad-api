package no.nav.sosialhjelp.soknad.bosituasjon

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val bosituasjonProxy: BosituasjonProxy,
) {
    @GetMapping
    fun hentBosituasjon(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return bosituasjonProxy.getBosituasjon(behandlingsId)
    }

    @PutMapping
    fun updateBosituasjon(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody bosituasjonFrontend: BosituasjonFrontend,
    ): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return bosituasjonProxy.updateBosituasjon(behandlingsId, bosituasjonFrontend)
    }

    data class BosituasjonFrontend(
        var botype: Botype?,
        var antallPersoner: Int?,
    )
}
