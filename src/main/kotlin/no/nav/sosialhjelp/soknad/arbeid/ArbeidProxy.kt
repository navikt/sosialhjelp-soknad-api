package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidController
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidsforholdDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArbeidProxy(private val arbeidController: ArbeidController) {
    fun getArbeid(soknadId: String) =
        arbeidController
            .getArbeid(UUID.fromString(soknadId))
            .toArbeidForholdResponse()

    fun updateArbeid(
        soknadId: String,
        arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest,
    ): ArbeidRessurs.ArbeidsforholdResponse {
        arbeidController.updateKommentarArbeidsforhold(
            soknadId = UUID.fromString(soknadId),
            input = ArbeidInput(arbeidFrontend.kommentarTilArbeidsforhold),
        )

        return arbeidController.getArbeid(UUID.fromString(soknadId)).toArbeidForholdResponse()
    }
}

private fun ArbeidDto.toArbeidForholdResponse() =
    ArbeidRessurs.ArbeidsforholdResponse(
        arbeidsforhold = arbeidsforholdList.map { it.toArbeidsforholdFrontend() },
        kommentarTilArbeidsforhold = kommentar,
    )

private fun ArbeidsforholdDto.toArbeidsforholdFrontend() =
    ArbeidRessurs.ArbeidsforholdFrontend(
        arbeidsgivernavn = arbeidsgivernavn,
        fom = start,
        tom = slutt,
        stillingstypeErHeltid = harFastStilling,
        stillingsprosent = fastStillingsprosent,
        overstyrtAvBruker = false,
    )
