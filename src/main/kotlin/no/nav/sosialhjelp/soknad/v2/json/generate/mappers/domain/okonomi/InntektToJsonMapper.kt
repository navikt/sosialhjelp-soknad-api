package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent

class InntektToJsonMapper(
    private val inntekter: Set<Inntekt>,
    private val bekreftelser: Set<Bekreftelse> = emptySet(),
) : OkonomiElementsToJsonMapper {
    override fun doMapping(jsonOkonomi: JsonOkonomi): JsonOkonomi =
        inntekter.fold(jsonOkonomi) { accumulated, inntekt -> inntekt.mapToJsonObject(accumulated) }
            .handleHusbankenSpecialCase()
            .let { accumulated ->
                inntekter.find { it.type == InntektType.UTBETALING_ANNET }
                    ?.let { accumulated.copy(opplysninger = accumulated.opplysninger.copy(beskrivelseAvAnnet = accumulated.opplysninger.beskrivelseAvAnnet!!.copy(utbetaling = it.beskrivelse ?: ""))) }
                    ?: accumulated
            }

    // Hvis bostotte == true && bostotte_samtykke == null || false skal kilde være bruker
    private fun JsonOkonomi.handleHusbankenSpecialCase(): JsonOkonomi {
        if (bekreftelser.find { it.type == BekreftelseType.BOSTOTTE }?.verdi == true) {
            if (bekreftelser.find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }?.verdi != true) {
                return copy(
                    opplysninger =
                        opplysninger.copy(
                            utbetaling =
                                opplysninger.utbetaling.map { utbetaling ->
                                    if (utbetaling.type == InntektType.UTBETALING_HUSBANKEN.toSoknadJsonTypeString()) utbetaling.copy(kilde = JsonKilde.BRUKER) else utbetaling
                                },
                        ),
                )
            }
        }
        return this
    }

    private fun Inntekt.mapToJsonObject(jsonOkonomi: JsonOkonomi): JsonOkonomi {
        when (type) {
            InntektType.BARNEBIDRAG_MOTTAR, InntektType.JOBB, InntektType.STUDIELAN_INNTEKT,
            -> return jsonOkonomi.oversikt!!.let { oversikt -> jsonOkonomi.copy(oversikt = oversikt.copy(inntekt = oversikt.inntekt + toJsonOversiktInntekter())) }
            else -> return jsonOkonomi.copy(opplysninger = jsonOkonomi.opplysninger.copy(utbetaling = jsonOkonomi.opplysninger.utbetaling + toJsonOpplysningUtbetalinger()))
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
    JsonOkonomioversiktInntekt(JsonKilde.BRUKER, type.toSoknadJsonTypeString(), toTittel(), false)
        .let { oversikt -> detalj?.addDetaljToOversiktForInntekt(oversikt) ?: oversikt }

private fun OkonomiDetalj.addDetaljToOversiktForInntekt(
    jsonInntekt: JsonOkonomioversiktInntekt,
): JsonOkonomioversiktInntekt {
    return when (this) {
        is Belop -> jsonInntekt.copy(brutto = belop?.toInt(), netto = belop?.toInt())
        is BruttoNetto -> jsonInntekt.copy(brutto = brutto?.toInt(), netto = netto?.toInt())
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
    return JsonOkonomiOpplysningUtbetaling(InntektTypeToKildeMapper.getKilde(type), type.toSoknadJsonTypeString(), toTittel(), false)
        .let { opplysning -> detalj?.addDetaljToOpplysningForInntekt(opplysning) ?: opplysning }
}

private fun InntektType.toSoknadJsonTypeString(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
        ?: error("Fant ikke mapping for InntektType: $this")
}

private fun OkonomiDetalj.addDetaljToOpplysningForInntekt(
    jsonUtbetaling: JsonOkonomiOpplysningUtbetaling,
): JsonOkonomiOpplysningUtbetaling {
    return when (this) {
        is Belop -> jsonUtbetaling.copy(belop = belop?.toInt())
        is UtbetalingMedKomponent -> addUtbetalingMedKomponent(jsonUtbetaling)
        is Utbetaling -> addUtbetaling(jsonUtbetaling)
        else -> error("Type: ${jsonUtbetaling.type} - Ugyldig detalj-type for Inntekt: ${this::class.simpleName}")
    }
}

private fun UtbetalingMedKomponent.addUtbetalingMedKomponent(jsonUtbetaling: JsonOkonomiOpplysningUtbetaling): JsonOkonomiOpplysningUtbetaling {
    //  Utbetaling med Komponent gjelder kun utbetaling fra NAV - der skal Belop være samme som netto
    return utbetaling.addUtbetaling(jsonUtbetaling).copy(belop = utbetaling.netto?.toInt(), tittel = tittel, komponenter = komponenter.map { it.toJsonKomponent() })
}

private fun Utbetaling.addUtbetaling(jsonUtbetaling: JsonOkonomiOpplysningUtbetaling): JsonOkonomiOpplysningUtbetaling =
    jsonUtbetaling.copy(brutto = brutto, netto = netto, belop = belop?.toInt(), skattetrekk = skattetrekk, andreTrekk = andreTrekk, utbetalingsdato = utbetalingsdato?.toString(), periodeFom = periodeFom?.toString(), periodeTom = periodeTom?.toString(), mottaker = mottaker?.toJsonMottaker(), organisasjon = organisasjon?.toJsonOrganisasjon())

private fun Organisasjon.toJsonOrganisasjon(): JsonOrganisasjon? {
    orgnummer?.let {
        if (it.matches(Regex("\\d{9}"))) {
            return JsonOrganisasjon(navn ?: "", orgnummer)
        }
    }
    return null
}

private fun Mottaker.toJsonMottaker(): JsonOkonomiOpplysningUtbetaling.Mottaker? {
    return JsonOkonomiOpplysningUtbetaling.Mottaker.entries.find { it.name == name }
}

private fun Komponent.toJsonKomponent() =
    JsonOkonomiOpplysningUtbetalingKomponent(type, belop, satsType, satsAntall, satsBelop)

private fun Inntekt.toTittel(): String {
    return when (type) {
        InntektType.BARNEBIDRAG_MOTTAR -> "Mottar Barnebidrag"
        InntektType.JOBB -> "Lønnsinntekt"
        InntektType.LONNSLIPP -> "Lønnsinntekt"
        InntektType.STUDIELAN_INNTEKT -> "Studielån og -stipend"
        InntektType.UTBETALING_FORSIKRING -> "Forsikringsutbetaling"
        InntektType.UTBETALING_ANNET -> "Annen utbetaling"
        InntektType.UTBETALING_UTBYTTE -> "Utbytte fra aksjer, obligasjoner eller fond"
        InntektType.UTBETALING_SALG -> "Solgt eiendom og/eller eiendel"
        InntektType.SLUTTOPPGJOER -> "Sluttoppgjør/feriepenger etter skatt"
        InntektType.UTBETALING_HUSBANKEN -> "Statlig bostøtte"
        InntektType.UTBETALING_SKATTEETATEN -> if (!beskrivelse.isNullOrBlank()) beskrivelse else "Lønnsinntekt"
        InntektType.UTBETALING_NAVYTELSE -> beskrivelse ?: ""
    }
}

private object InntektTypeToKildeMapper {
    fun getKilde(inntektType: InntektType): JsonKilde =
        if (typerFraRegister.any { it == inntektType }) {
            JsonKilde.SYSTEM
        } else {
            JsonKilde.BRUKER
        }

    private val typerFraRegister =
        listOf(
            InntektType.UTBETALING_HUSBANKEN,
            InntektType.UTBETALING_NAVYTELSE,
            InntektType.UTBETALING_SKATTEETATEN,
        )
}
