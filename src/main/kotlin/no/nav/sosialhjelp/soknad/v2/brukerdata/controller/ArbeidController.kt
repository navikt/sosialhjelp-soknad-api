package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
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
//@ProtectionSelvbetjeningHigh
@Unprotected
@RequestMapping("/soknad/{soknadId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidController(
    private val soknadService: SoknadService,
    private val brukerdataService: BrukerdataService
) {

    @GetMapping
    fun getArbeid(@PathVariable("soknadId") soknadId: UUID): ArbeidDto {
        val arbeidsforhold = soknadService.getArbeidsforhold(soknadId)
        val kommentarArbeidsforhold = brukerdataService.getBrukerdataFormelt(soknadId)?.kommentarArbeidsforhold

        return ArbeidDto(
            arbeidsforholdList = arbeidsforhold,
            kommentar = kommentarArbeidsforhold
        )
    }

    @PutMapping
    fun updateKommentarArbeidsforhold(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody kommentarTilArbeidsforhold: String
    ): ArbeidDto {
        validate(kommentarTilArbeidsforhold)

        return ArbeidDto(
            kommentar = brukerdataService.updateKommentarArbeidsforhold(soknadId, kommentarTilArbeidsforhold),
            arbeidsforholdList = soknadService.getArbeidsforhold(soknadId)
        )
    }
}

fun validate(tekst: String) {
    tekst.toList()
        .forEach {
            if (!it.isLetterOrDigit()) {
                throw IllegalArgumentException("Kommentar til arbeidsforhold inneholder ikke kun bokstaver og tall.")
            }
        }
}

data class ArbeidDto(
    val arbeidsforholdList: Set<Arbeidsforhold>,
    val kommentar: String? = null
)
