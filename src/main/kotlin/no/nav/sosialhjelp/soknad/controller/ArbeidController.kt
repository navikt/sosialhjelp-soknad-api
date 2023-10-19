package no.nav.sosialhjelp.soknad.controller

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.model.ArbeidDto
import no.nav.sosialhjelp.soknad.model.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.model.arbeid.Arbeid
import no.nav.sosialhjelp.soknad.model.arbeid.Arbeidsforhold
import no.nav.sosialhjelp.soknad.service.ArbeidService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/soknad/{soknadId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArbeidController(
    private val arbeidService: ArbeidService
) {
    
    @GetMapping
    fun hentArbeid(
        @PathVariable("soknadId") soknadId: UUID
    ): ArbeidDto {
        return arbeidService.hentArbeid(soknadId)?.toDto()
            ?: throw IkkeFunnetException(melding = "Fant ikke Arbeid")
    }
    
    @PutMapping
    fun updateArbeid(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody arbeidDto: ArbeidDto
    ): ArbeidDto {
        return arbeidService.updateArbeid(
            soknadId = soknadId,
            kommentarArbeid = arbeidDto.kommentarArbeid
        ).toDto()
    }
}

fun Arbeid.toDto(): ArbeidDto = ArbeidDto (
    kommentarArbeid = kommentarArbeid,
    arbeidsforhold = arbeidsforhold.map {
        it.toDto()
    }.toSet()
)

fun Arbeidsforhold.toDto(): ArbeidsforholdDto {
    return ArbeidsforholdDto (
        arbeidsgivernavn = arbeidsgivernavn,
        orgnummer = orgnummer,
        fraOgMed = fraOgMed.toString(),
        tilOgMed = tilOgMed.toString(),
        stillingstype = stillingstype,
        stillingsprosent = stillingsprosent
    )
}
