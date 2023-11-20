package no.nav.sosialhjelp.soknad.soknad.controller

import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidRequest
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidResponse
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.nymodell.service.BrukerdataService
import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
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
    private val brukerdataService: BrukerdataService,
    private val livssituasjonService: LivssituasjonService
) {

    @GetMapping
    fun hentArbeid(
        @PathVariable("soknadId") soknadId: UUID
    ): ArbeidResponse {
        return ArbeidResponse(
            kommentarArbeid = brukerdataService.hentKommentarArbeidsforhold(soknadId),
            arbeidsforhold = livssituasjonService.hentArbeidsforhold(soknadId)
                .map { it.toResponse() }
        )
    }

    @PutMapping
    fun updateArbeid(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody arbeidRequest: ArbeidRequest
    ): ArbeidResponse {
        val kommentar = brukerdataService.updateKommentarArbeidsforhold(soknadId, arbeidRequest.kommentarArbeid)
        val arbeidsforhold = livssituasjonService.hentArbeidsforhold(soknadId)

        return ArbeidResponse(
            kommentarArbeid = kommentar,
            arbeidsforhold = arbeidsforhold.map { it.toResponse() }
        )
    }

    private fun Arbeidsforhold.toResponse() = ArbeidsforholdDto(
        arbeidsgivernavn = arbeidsgivernavn,
        fraOgMed = fraOgMed.toString(),
        tilOgMed = tilOgMed.toString(),
        stillingstype = stillingstype,
        stillingsprosent = stillingsprosent
    )
}
