package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.booleanVerdiFelt
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue

class AndreInntekter {
    fun getAvsnitt(opplysninger: JsonOkonomiopplysninger): Avsnitt {
        return Avsnitt(
            tittel = "inntekt.inntekter.titel",
            sporsmal = andreInntekterSporsmal(opplysninger),
        )
    }

    private fun andreInntekterSporsmal(opplysninger: JsonOkonomiopplysninger): List<Sporsmal> {
        val harUtfyltAndreInntekterSporsmal = harBekreftelse(opplysninger, BEKREFTELSE_UTBETALING)
        val harSvartJaAndreInntekter = harBekreftelseTrue(opplysninger, BEKREFTELSE_UTBETALING)
        val utbetalingTyper = listOf(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING, UTBETALING_ANNET)
        val sporsmal = mutableListOf<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = "inntekt.inntekter.sporsmal",
                erUtfylt = harUtfyltAndreInntekterSporsmal,
                felt =
                    if (harUtfyltAndreInntekterSporsmal) {
                        booleanVerdiFelt(
                            harSvartJaAndreInntekter,
                            "inntekt.inntekter.true",
                            "inntekt.inntekter.false",
                        )
                    } else {
                        null
                    },
            ),
        )
        if (harSvartJaAndreInntekter) {
            val harSvartHvaHarDuMottattSporsmal = opplysninger.utbetaling.any { utbetalingTyper.contains(it.type) }
            sporsmal.add(
                Sporsmal(
                    tittel = "inntekt.inntekter.true.type.sporsmal",
                    erUtfylt = harSvartHvaHarDuMottattSporsmal,
                    felt = if (harSvartHvaHarDuMottattSporsmal) andreinntekterFelter(opplysninger) else null,
                ),
            )
            if (sporsmal[1].containsFeltWithSvar("json.okonomi.opplysninger.inntekt.inntekter.annet")) {
                val beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet
                val harUtfyltAnnetFelt =
                    beskrivelseAvAnnet != null && beskrivelseAvAnnet.utbetaling != null && beskrivelseAvAnnet.utbetaling.isNotBlank()
                sporsmal.add(
                    Sporsmal(
                        tittel = "inntekt.inntekter.true.type.annet",
                        erUtfylt = harUtfyltAnnetFelt,
                        felt =
                            beskrivelseAvAnnet?.let {
                                if (harUtfyltAnnetFelt) {
                                    listOf(
                                        Felt(
                                            type = Type.TEKST,
                                            svar = createSvar(it.utbetaling, SvarType.TEKST),
                                        ),
                                    )
                                } else {
                                    null
                                }
                            },
                    ),
                )
            }
        }
        return sporsmal
    }

    private fun andreinntekterFelter(opplysninger: JsonOkonomiopplysninger): List<Felt> {
        val felter = mutableListOf<Felt>()
        addUtbetalingIfPresent(
            opplysninger,
            felter,
            UTBETALING_UTBYTTE,
            "json.okonomi.opplysninger.inntekt.inntekter.utbytte",
        )
        addUtbetalingIfPresent(
            opplysninger,
            felter,
            UTBETALING_SALG,
            "json.okonomi.opplysninger.inntekt.inntekter.salg",
        )
        addUtbetalingIfPresent(
            opplysninger,
            felter,
            UTBETALING_FORSIKRING,
            "json.okonomi.opplysninger.inntekt.inntekter.forsikringsutbetalinger",
        )
        addUtbetalingIfPresent(
            opplysninger,
            felter,
            UTBETALING_ANNET,
            "json.okonomi.opplysninger.inntekt.inntekter.annet",
        )
        return felter
    }

    private fun addUtbetalingIfPresent(
        opplysninger: JsonOkonomiopplysninger,
        felter: MutableList<Felt>,
        type: String,
        key: String,
    ) {
        opplysninger.utbetaling.firstOrNull { type == it.type }?.let {
            felter.add(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar(key, SvarType.LOCALE_TEKST),
                ),
            )
        }
    }
}
