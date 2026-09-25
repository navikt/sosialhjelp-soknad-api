package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenter
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType

class UtgiftToJsonMapper(
    private val utgifter: Set<Utgift>,
) : OkonomiElementsToJsonMapper {
    override fun doMapping(jsonOkonomi: JsonOkonomi): JsonOkonomi =
        utgifter.fold(jsonOkonomi.copy(opplysninger = jsonOkonomi.opplysninger.copy(utgift = emptyList()))) { accumulated, utgift ->
            utgift.mapToJsonObject(accumulated)
        }

    private fun Utgift.mapToJsonObject(jsonOkonomi: JsonOkonomi): JsonOkonomi {
        when (type) {
            UtgiftType.BARNEBIDRAG_BETALER, UtgiftType.UTGIFTER_SFO, UtgiftType.UTGIFTER_BARNEHAGE,
            UtgiftType.UTGIFTER_HUSLEIE, UtgiftType.UTGIFTER_BOLIGLAN, UtgiftType.UTGIFTER_BOLIGLAN_RENTER,
            UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG,
            -> return jsonOkonomi.oversikt!!.let { oversikt -> jsonOkonomi.copy(oversikt = oversikt.copy(utgift = oversikt.utgift.orEmpty() + toJsonOversiktUtgifter())) }
            else -> return jsonOkonomi.copy(opplysninger = jsonOkonomi.opplysninger.copy(utgift = jsonOkonomi.opplysninger.utgift.orEmpty() + toJsonOpplysningUtgifter()))
        }
    }

    private fun Utgift.toJsonOversiktUtgifter(): List<JsonOkonomioversiktUtgift> {
        return utgiftDetaljer.detaljer.let { detaljer ->
            if (detaljer.isEmpty()) {
                listOf(toJsonOversiktUtgift())
            } else {
                detaljer.flatMap { detalj ->
                    when (detalj) {
                        is AvdragRenter -> this.copy().handleAvdragRenter(detalj)
                        else -> listOf(this.copy().toJsonOversiktUtgift(detalj as Belop))
                    }
                }
            }
        }
    }

    private fun Utgift.handleAvdragRenter(detalj: AvdragRenter): List<JsonOkonomioversiktUtgift> {
        return listOf(
            JsonOkonomioversiktUtgift(JsonKilde.BRUKER, UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG.toSoknadJsonTypeString(), toTittel(), false, detalj.avdrag?.toInt()),
            JsonOkonomioversiktUtgift(JsonKilde.BRUKER, UtgiftType.UTGIFTER_BOLIGLAN_RENTER.toSoknadJsonTypeString(), toTittel(), false, detalj.renter?.toInt()),
        )
    }

    private fun UtgiftType.toSoknadJsonTypeString(): String {
        return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
            ?: error("Fant ikke mapping for UtgiftType: $this")
    }

    private fun Utgift.toJsonOversiktUtgift(belop: Belop? = null) =
        JsonOkonomioversiktUtgift(JsonKilde.BRUKER, type.toSoknadJsonTypeString(), toTittel(), false, belop?.belop?.toInt())

    private fun Utgift.toJsonOpplysningUtgifter(): List<JsonOkonomiOpplysningUtgift> {
        // Hvis bruker ikke har lagt til andre utgifter, så skal det ikke opprettes en tom opplysning.
        return utgiftDetaljer.detaljer.let { detaljer ->
            when {
                detaljer.isEmpty() && UtgiftType.UTGIFTER_ANDRE_UTGIFTER == type -> emptyList()
                detaljer.isEmpty() -> listOf(toJsonOpplysningUtgift())
                else ->
                    detaljer.map { detalj ->
                        this.copy().toJsonOpplysningUtgift(detalj as Belop, detalj.beskrivelse)
                    }
            }
        }
    }

    private fun Utgift.toJsonOpplysningUtgift(
        belop: Belop? = null,
        detaljBeskrivelse: String? = null,
    ) =
        JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, type.toSoknadJsonTypeString(), toTittel(detaljBeskrivelse), false, belop?.belop?.toInt())
}

private fun Utgift.toTittel(detaljBeskrivelse: String? = null): String {
    return when (type) {
        UtgiftType.UTGIFTER_ANNET_BO -> "Annen, bo (brukerangitt): ${detaljBeskrivelse ?: ""}"
        UtgiftType.UTGIFTER_ANNET_BARN -> "Annen, barn(brukerangitt): ${detaljBeskrivelse ?: ""}"
        UtgiftType.UTGIFTER_BARN_TANNREGULERING -> "Tannregulering for barn (siste regning)"
        UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT -> "Kommunal avgift (siste regning)"
        UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER -> "Fritidsaktiviteter for barn (siste regning): ${detaljBeskrivelse ?: ""}"
        UtgiftType.UTGIFTER_OPPVARMING -> "Oppvarming (siste regning)"
        UtgiftType.UTGIFTER_STROM -> "Strøm (siste regning)"
        UtgiftType.UTGIFTER_ANDRE_UTGIFTER -> "Annen (brukerangitt): ${detaljBeskrivelse ?: ""}"
        UtgiftType.BARNEBIDRAG_BETALER -> "Betaler Barnebidrag"
        UtgiftType.UTGIFTER_SFO -> "SFO"
        UtgiftType.UTGIFTER_BARNEHAGE -> "Barnehage"
        UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG -> "Avdrag på boliglån"
        UtgiftType.UTGIFTER_BOLIGLAN_RENTER -> "Renter på boliglån"
        UtgiftType.UTGIFTER_BOLIGLAN -> "Renter og avdrag på boliglån"
        UtgiftType.UTGIFTER_HUSLEIE -> "Husleie"
    }
}
