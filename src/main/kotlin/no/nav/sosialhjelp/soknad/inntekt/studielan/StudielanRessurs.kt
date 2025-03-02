package no.nav.sosialhjelp.soknad.inntekt.studielan

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
@RequestMapping("/soknader/{behandlingsId}/inntekt/studielan", produces = [MediaType.APPLICATION_JSON_VALUE])
class StudielanRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val studielanProxy: StudielanProxy,
) {
    @GetMapping
    fun hentStudielanBekreftelse(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): StudielanFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return studielanProxy.getStudielan(behandlingsId)
    }

    @PutMapping
    fun updateStudielan(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody studielanFrontend: StudielanInputDTO,
    ): StudielanFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return studielanProxy.leggTilStudielan(behandlingsId, studielanFrontend)
    }

    // TODO: Disse klassene har veldig dårlige navn.
    // TODO Skal backend bestemme om inntekt studielan skal vises?
    data class StudielanFrontend(
        /** Søker er student */
        val skalVises: Boolean,
        /** Søker mottar lån eller stipend fra Lånekassen */
        val bekreftelse: Boolean?,
    )

    data class StudielanInputDTO(
        /** Søker mottar lån eller stipend fra Lånekassen */
        val bekreftelse: Boolean?,
    )
}
