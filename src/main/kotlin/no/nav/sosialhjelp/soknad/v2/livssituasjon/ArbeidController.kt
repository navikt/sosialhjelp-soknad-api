package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.security.token.support.core.api.Unprotected
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
    private val livssituasjonService: LivssituasjonService,
) {

    @GetMapping
    fun getArbeid(@PathVariable("soknadId") soknadId: UUID): ArbeidDto {
        return livssituasjonService.getLivssituasjon(soknadId)?.let {
            ArbeidDto(
                arbeidsforholdList = it.arbeid.arbeidsforhold.map { list -> list.toArbeidsforholdDto() },
                kommentar = it.arbeid.kommentar
            )
        } ?: ArbeidDto()
    }

    @PutMapping
    fun updateKommentarArbeidsforhold(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: ArbeidInput
    ): ArbeidDto {
        return livssituasjonService.updateArbeid(soknadId, input.kommentarTilArbeidsforhold)
            .let {
                ArbeidDto(
                    arbeidsforholdList = it.arbeidsforhold.map { list -> list.toArbeidsforholdDto() },
                    kommentar = it.kommentar
                )
            }
    }
}

data class ArbeidDto(
    val arbeidsforholdList: List<ArbeidsforholdDto> = emptyList(),
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
