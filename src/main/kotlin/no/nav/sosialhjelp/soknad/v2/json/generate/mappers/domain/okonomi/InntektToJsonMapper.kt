package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
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
) : OkonomiElementsToJsonMapper {
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
            detaljer.map { this.copy().toJsonOversiktInntekt(it) }
        }
    }
}

private fun Inntekt.toJsonOversiktInntekt(detalj: OkonomiDetalj? = null) =
    JsonOkonomioversiktInntekt()
        // TODO Typene må mappes til Kilde
        .withKilde(JsonKilde.BRUKER)
        .withType(type.toSoknadJsonTypeString())
        .withTittel(toTittel())
        .withOverstyrtAvBruker(false)
        .let { oversikt -> detalj?.addDetaljToOversiktForInntekt(oversikt) ?: oversikt }

private fun OkonomiDetalj.addDetaljToOversiktForInntekt(
    jsonInntekt: JsonOkonomioversiktInntekt,
): JsonOkonomioversiktInntekt {
    return when (this) {
        is Belop -> jsonInntekt.withBrutto(belop.toInt()).withNetto(belop.toInt())
        is BruttoNetto -> jsonInntekt.withBrutto(brutto?.toInt()).withNetto(netto?.toInt())
        else -> error("Ugyldig OkonomiDetalj-type for Oversikt Inntekt")
    }
}

private fun Inntekt.toJsonOpplysningUtbetalinger(): List<JsonOkonomiOpplysningUtbetaling> {
    return inntektDetaljer.detaljer.let { detaljer ->
        when (detaljer.isEmpty()) {
            true -> listOf(toJsonOpplysingUtbetaling())
            false -> detaljer.map { this.copy().toJsonOpplysingUtbetaling(it) }
        }
    }
}

private fun Inntekt.toJsonOpplysingUtbetaling(detalj: OkonomiDetalj? = null): JsonOkonomiOpplysningUtbetaling {
    return JsonOkonomiOpplysningUtbetaling()
        // TODO Kilder må håndteres da de kan være både SYSTEM og BRUKER
        // TODO For de fleste opplysningstypene vil det enkleste være mapping pr. OpplysningType
        .withKilde(JsonKilde.BRUKER)
        .withType(type.toSoknadJsonTypeString())
        .withTittel(toTittel())
        .withOverstyrtAvBruker(false)
        .let { opplysning -> detalj?.addDetaljToOpplysningForInntekt(opplysning) ?: opplysning }
}

private fun InntektType.toSoknadJsonTypeString(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
        ?: error("Fant ikke mapping for InntektType: $this")
}

private fun OkonomiDetalj.addDetaljToOpplysningForInntekt(
    jsonUtbetaling: JsonOkonomiOpplysningUtbetaling,
): JsonOkonomiOpplysningUtbetaling {
    when (this) {
        is Belop -> jsonUtbetaling.withBelop(this.belop.toInt())
        is UtbetalingMedKomponent -> addUtbetalingMedKomponent(jsonUtbetaling)
        is Utbetaling -> addUtbetaling(jsonUtbetaling)
        else -> error("Type: ${jsonUtbetaling.type} - Ugyldig detalj-type for Inntekt: ${this::class.simpleName}")
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
        // TODO Utbetaling husbanken har hatt en Utbetaling når BOSTOTTE har vært true...
        // TODO ...men allikevel ikke hvis SAMTYKKE også er true.
        InntektType.UTBETALING_HUSBANKEN -> "Statlig bostøtte"
        // TODO UTBETALING_SKATTEETATEN bevarer tittel innhentingen
        InntektType.UTBETALING_SKATTEETATEN -> beskrivelse ?: ""
        // TODO UTBETALING_NAVYTELSE bevarer tittel innhentingen
        InntektType.UTBETALING_NAVYTELSE -> beskrivelse ?: ""
    }
}
