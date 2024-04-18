package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.getBekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue

class SkattbarInntekt {
    fun getAvsnitt(
        okonomi: JsonOkonomi,
        driftsinformasjon: JsonDriftsinformasjon,
    ): Avsnitt {
        val opplysninger = okonomi.opplysninger
        val fikkFeilMotSkatteetaten = java.lang.Boolean.TRUE == driftsinformasjon.inntektFraSkatteetatenFeilet
        return Avsnitt(
            tittel = "utbetalinger.inntekt.skattbar.tittel",
            sporsmal = skattbarInntektSporsmal(opplysninger, fikkFeilMotSkatteetaten),
        )
    }

    private fun skattbarInntektSporsmal(
        opplysninger: JsonOkonomiopplysninger,
        fikkFeilMotSkatteetaten: Boolean,
    ): List<Sporsmal> {
        val harSkatteetatenSamtykke = harBekreftelseTrue(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE)
        val sporsmal = mutableListOf<Sporsmal>()
        if (!harSkatteetatenSamtykke) {
            sporsmal.add(
                Sporsmal(
                    tittel = "utbetalinger.inntekt.skattbar.mangler_samtykke",
                    erUtfylt = true,
                    felt = null,
                ),
            )
        }
        if (harSkatteetatenSamtykke && fikkFeilMotSkatteetaten) {
            sporsmal.add(
                Sporsmal(
                    tittel = "utbetalinger.skattbar.kontaktproblemer.oppsummering",
                    erUtfylt = true,
                    felt = null,
                ),
            )
        }
        if (harSkatteetatenSamtykke && !fikkFeilMotSkatteetaten) {
            getBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE)
                ?.let { bekreftelsesTidspunktSporsmal(it) }
                ?.let { sporsmal.add(it) }

            sporsmal.add(
                Sporsmal(
                    tittel = "utbetalinger.inntekt.skattbar.inntekt.tittel",
                    erUtfylt = true,
                    felt = skattbarInntektFelter(opplysninger.utbetaling),
                ),
            )
        }
        return sporsmal
    }

    private fun bekreftelsesTidspunktSporsmal(skatteetatenBekreftelse: JsonOkonomibekreftelse): Sporsmal {
        return Sporsmal(
            tittel = "utbetalinger.inntekt.skattbar.har_gitt_samtykke",
            erUtfylt = true,
            felt =
                listOf(
                    Felt(
                        type = Type.TEKST,
                        svar = createSvar(skatteetatenBekreftelse.bekreftelsesDato, SvarType.TIDSPUNKT),
                    ),
                ),
        )
    }

    private fun skattbarInntektFelter(utbetalinger: List<JsonOkonomiOpplysningUtbetaling>?): List<Felt> {
        val harSkattbareInntekter = utbetalinger != null && utbetalinger.any { UTBETALING_SKATTEETATEN == it.type }
        val feltListe = mutableListOf<Felt>()
        if (!harSkattbareInntekter) {
            feltListe.add(
                Felt(
                    type = Type.TEKST,
                    svar = createSvar("utbetalinger.inntekt.skattbar.ingen", SvarType.LOCALE_TEKST),
                ),
            )
        } else {
            utbetalinger
                ?.filter { UTBETALING_SKATTEETATEN == it.type }
                ?.forEach {
                    val map = LinkedHashMap<String, Svar>()
                    if (it.organisasjon == null) {
                        map["utbetalinger.utbetaling.arbeidsgivernavn.label"] =
                            createSvar("Uten organisasjonsnummer", SvarType.TEKST)
                    } else {
                        map["utbetalinger.utbetaling.arbeidsgivernavn.label"] =
                            createSvar(it.organisasjon.navn, SvarType.TEKST)
                    }
                    map["utbetalinger.utbetaling.periodeFom.label"] = createSvar(it.periodeFom, SvarType.DATO)
                    map["utbetalinger.utbetaling.periodeTom.label"] = createSvar(it.periodeTom, SvarType.DATO)
                    map["utbetalinger.utbetaling.brutto.label"] = createSvar(it.brutto.toString(), SvarType.TEKST)
                    map["utbetalinger.utbetaling.skattetrekk.label"] =
                        createSvar(it.skattetrekk.toString(), SvarType.TEKST)
                    feltListe.add(
                        Felt(
                            type = Type.SYSTEMDATA_MAP,
                            labelSvarMap = map,
                        ),
                    )
                }
        }
        return feltListe
    }
}
