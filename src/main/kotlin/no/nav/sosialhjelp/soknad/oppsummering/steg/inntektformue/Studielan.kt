package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.booleanVerdiFelt
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue

class Studielan {
    fun getAvsnitt(opplysninger: JsonOkonomiopplysninger): Avsnitt {
        return Avsnitt(
            tittel = "inntekt.studielan.tittel",
            sporsmal = studielanSporsmal(opplysninger),
        )
    }

    private fun studielanSporsmal(opplysninger: JsonOkonomiopplysninger): List<Sporsmal> {
        val harUtfyltStudielanSporsmal = harBekreftelse(opplysninger, SoknadJsonTyper.STUDIELAN)
        val harSvartJaStudielan =
            harUtfyltStudielanSporsmal && harBekreftelseTrue(opplysninger, SoknadJsonTyper.STUDIELAN)
        return listOf(
            Sporsmal(
                tittel = "inntekt.studielan.sporsmal",
                erUtfylt = harUtfyltStudielanSporsmal,
                felt =
                    if (harUtfyltStudielanSporsmal) {
                        booleanVerdiFelt(
                            harSvartJaStudielan,
                            "inntekt.studielan.true",
                            "inntekt.studielan.false",
                        )
                    } else {
                        null
                    },
            ),
        )
    }
}
