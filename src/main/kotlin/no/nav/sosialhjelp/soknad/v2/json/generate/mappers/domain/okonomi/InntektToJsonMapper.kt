package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType

class InntektToJsonMapper(
    private val inntekter: Set<Inntekt>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiDelegateMapper {
    private val oversikt = jsonOkonomi.oversikt
    private val opplysninger = jsonOkonomi.opplysninger

    override fun doMapping() {
        inntekter.forEach { it.mapToJsonObject() }

        inntekter.find { it.type == InntektType.UTBETALING_ANNET }
            ?.let {
                val jsonBeskrivelser = opplysninger.beskrivelseAvAnnet ?: opplysninger.initJsonBeskrivelser()
                jsonBeskrivelser.utbetaling = it.beskrivelse ?: ""
            }
    }

    private fun Inntekt.mapToJsonObject() {
        when (type) {
            InntektType.BARNEBIDRAG_MOTTAR, InntektType.JOBB, InntektType.STUDIELAN_INNTEKT,
            -> oversikt.inntekt.addAll(toJsonOversiktInntekter())
            else -> opplysninger.utbetaling.addAll(toJsonOpplysningUtbetalinger())
        }
    }
}

private fun Inntekt.toJsonOversiktInntekter(): List<JsonOkonomioversiktInntekt> {
    return inntektDetaljer.detaljer.let { detaljer ->
        if (detaljer.isEmpty()) {
            listOf(toJsonOversiktInntekt())
        } else {
            detaljer.map { this.copy().toJsonOversiktInntekt(it as BruttoNetto) }
        }
    }
}

private fun Inntekt.toJsonOversiktInntekt(detalj: BruttoNetto? = null) =
    JsonOkonomioversiktInntekt()
        // TODO Typene må mappes til Kilde
        .withKilde(JsonKilde.BRUKER)
        .withType(type.name)
        .withTittel(toTittel())
        .withBrutto(detalj?.brutto?.toInt())
        .withNetto(detalj?.netto?.toInt())
        .withOverstyrtAvBruker(false)

private fun Inntekt.toJsonOpplysningUtbetalinger(): List<JsonOkonomiOpplysningUtbetaling> {
    return inntektDetaljer.detaljer.let { detaljer ->
        if (detaljer.isEmpty()) {
            listOf(toJsonOpplysingUtbetaling())
        } else {
            detaljer.map { this.copy().toJsonOpplysingUtbetaling(it) }
        }
    }
}

private fun Inntekt.toJsonOpplysingUtbetaling(detalj: OkonomiDetalj? = null): JsonOkonomiOpplysningUtbetaling {
    return JsonOkonomiOpplysningUtbetaling()
        // TODO Kilder må håndteres da de kan være både SYSTEM og BRUKER
        .withKilde(JsonKilde.BRUKER)
        .withType(type.name)
        .withTittel(toTittel())
        .withOverstyrtAvBruker(false)
        .let { detalj?.addOkonomiskeDetaljer(it) ?: it }
}

private fun OkonomiDetalj.addOkonomiskeDetaljer(
    jsonUtbetaling: JsonOkonomiOpplysningUtbetaling,
): JsonOkonomiOpplysningUtbetaling {
    when (this) {
        is UtbetalingMedKomponent -> addUtbetalingMedKomponent(jsonUtbetaling)
        is Utbetaling -> addUtbetaling(jsonUtbetaling)
    }
    return jsonUtbetaling
}

private fun UtbetalingMedKomponent.addUtbetalingMedKomponent(jsonUtbetaling: JsonOkonomiOpplysningUtbetaling) {
    utbetaling.addUtbetaling(jsonUtbetaling)
    komponenter.map { it.toJsonKomponent() }.let { jsonUtbetaling.withKomponenter(it) }
}

private fun Utbetaling.addUtbetaling(jsonUtbetaling: JsonOkonomiOpplysningUtbetaling) {
    jsonUtbetaling
        .withBrutto(brutto)
        .withNetto(netto)
        .withBelop(belop?.toInt())
        .withSkattetrekk(skattetrekk)
        .withAndreTrekk(andreTrekk)
        .withUtbetalingsdato(utbetalingsdato?.toString())
        .withPeriodeFom(periodeFom?.toString())
        .withPeriodeTom(periodeTom?.toString())
        .withMottaker(mottaker?.toJsonMottaker())
}

private fun Mottaker.toJsonMottaker(): JsonOkonomiOpplysningUtbetaling.Mottaker? {
    return JsonOkonomiOpplysningUtbetaling.Mottaker.entries.find { it.name == name }
}

private fun Komponent.toJsonKomponent() =
    JsonOkonomiOpplysningUtbetalingKomponent()
        .withType(type)
        .withBelop(belop)
        .withSatsType(satsType)
        .withSatsBelop(satsBelop)
        .withSatsAntall(satsAntall)

private fun Inntekt.toTittel(): String {
    return when (type) {
        InntektType.BARNEBIDRAG_MOTTAR -> "Mottar Barnebidrag"
        InntektType.JOBB -> "Lønnsinntekt"
        InntektType.STUDIELAN_INNTEKT -> "Studielån og -stipend"
        InntektType.UTBETALING_FORSIKRING -> "Forsikringsutbetaling"
        InntektType.UTBETALING_ANNET -> "Annen utbetaling"
        InntektType.UTBETALING_UTBYTTE -> "Utbytte fra aksjer, obligasjoner eller fond"
        InntektType.UTBETALING_SALG -> "Solgt eiendom og/eller eiendel"
        InntektType.SLUTTOPPGJOER -> "Sluttoppgjør/feriepenger etter skatt"
        InntektType.UTBETALING_HUSBANKEN -> "Statlig bostøtte"
        // TODO UTBETALING_SKATTEETATEN bevarer tittel innhentingen
        InntektType.UTBETALING_SKATTEETATEN -> beskrivelse ?: ""
        // TODO UTBETALING_NAVYTELSE bevarer tittel innhentingen
        InntektType.UTBETALING_NAVYTELSE -> beskrivelse ?: ""
    }
}
