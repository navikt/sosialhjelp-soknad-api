package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataService
import no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
// @ProtectionSelvbetjeningHigh
@Unprotected
@RequestMapping("/soknad/{soknadId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidController(
    private val soknadService: SoknadService,
    private val brukerdataService: BrukerdataService
) {

    @GetMapping
    fun getArbeid(@PathVariable("soknadId") soknadId: UUID): ArbeidDto {
        val arbeidsforhold = soknadService.getSoknad(soknadId).arbeidsForhold
        val kommentarArbeidsforhold = brukerdataService.getBrukerdataFormelt(soknadId)?.kommentarArbeidsforhold

        return ArbeidDto(
            arbeidsforholdList = arbeidsforhold.map { it.toArbeidsforholdDto() },
            kommentar = kommentarArbeidsforhold
        )
    }

    @PutMapping
    fun updateKommentarArbeidsforhold(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: ArbeidInput
    ): ArbeidDto {
        SoknadInputValidator(ArbeidInput::class)
            .validateTextInput(soknadId, input.kommentarTilArbeidsforhold)

        return ArbeidDto(
            kommentar = brukerdataService.updateKommentarArbeidsforhold(soknadId, input.kommentarTilArbeidsforhold),
            arbeidsforholdList = soknadService.getSoknad(soknadId)
                .arbeidsForhold.map { it.toArbeidsforholdDto() }
        )
    }
}

data class ArbeidDto(
    val arbeidsforholdList: List<ArbeidsforholdDto>,
    val kommentar: String? = null
)

data class ArbeidInput(
    val kommentarTilArbeidsforhold: String
)

data class ArbeidsforholdDto(
    val arbeidsgivernavn: String? = null,
    val orgnummer: String? = null,
    val start: String? = null,
    val slutt: String? = null,
    val fastStillingsprosent: Int? = null,
    val harFastStilling: Boolean? = null
)

private fun Arbeidsforhold.toArbeidsforholdDto(): ArbeidsforholdDto {
    return ArbeidsforholdDto(
        arbeidsgivernavn = arbeidsgivernavn,
        orgnummer = orgnummer,
        start = start,
        slutt = slutt,
        fastStillingsprosent = fastStillingsprosent,
        harFastStilling = harFastStilling
    )
}
