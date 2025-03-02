package no.nav.sosialhjelp.soknad.arbeid

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
@RequestMapping("/soknader/{behandlingsId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val arbeidProxy: ArbeidProxy,
) {
    @GetMapping
    fun hentArbeid(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): ArbeidsforholdResponse {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return arbeidProxy.getArbeid(behandlingsId)
    }

    @PutMapping
    fun updateArbeid(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody arbeidFrontend: ArbeidsforholdRequest,
    ): ArbeidsforholdResponse {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return arbeidProxy.updateArbeid(behandlingsId, arbeidFrontend)
    }

    data class ArbeidsforholdResponse(
        val arbeidsforhold: List<ArbeidsforholdFrontend>,
        val kommentarTilArbeidsforhold: String?,
    )

    data class ArbeidsforholdRequest(
        val kommentarTilArbeidsforhold: String?,
    )

    data class ArbeidsforholdFrontend(
        var arbeidsgivernavn: String?,
        var fom: String?,
        var tom: String?,
        var stillingstypeErHeltid: Boolean?,
        var stillingsprosent: Int?,
        var overstyrtAvBruker: Boolean?,
    )
}
