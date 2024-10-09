package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenter
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType

class UtgiftToJsonMapper(
    private val utgifter: Set<Utgift>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiElementsToJsonMapper {
    private val oversikt = jsonOkonomi.oversikt
    private val opplysninger = jsonOkonomi.opplysninger

    override fun doMapping() {
        opplysninger.withUtgift(mutableListOf())
        utgifter.forEach { it.mapToJsonObject() }
    }

    private fun Utgift.mapToJsonObject() {
        when (type) {
            UtgiftType.BARNEBIDRAG_BETALER, UtgiftType.UTGIFTER_SFO, UtgiftType.UTGIFTER_BARNEHAGE,
            UtgiftType.UTGIFTER_HUSLEIE, UtgiftType.UTGIFTER_BOLIGLAN, UtgiftType.UTGIFTER_BOLIGLAN_RENTER,
            UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG,
            -> oversikt.utgift.addAll(toJsonOversiktUtgifter())
            else -> opplysninger.utgift.addAll(toJsonOpplysningUtgifter())
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
            JsonOkonomioversiktUtgift()
                .withKilde(JsonKilde.BRUKER)
                .withType(UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG.toSoknadJsonTypeString())
                .withTittel(toTittel())
                .withBelop(detalj.avdrag?.toInt())
                .withOverstyrtAvBruker(false),
            JsonOkonomioversiktUtgift()
                .withKilde(JsonKilde.BRUKER)
                .withType(UtgiftType.UTGIFTER_BOLIGLAN_RENTER.toSoknadJsonTypeString())
                .withTittel(toTittel())
                .withBelop(detalj.renter?.toInt())
                .withOverstyrtAvBruker(false),
        )
    }

    private fun UtgiftType.toSoknadJsonTypeString(): String {
        return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
            ?: error("Fant ikke mapping for UtgiftType: $this")
    }

    private fun Utgift.toJsonOversiktUtgift(belop: Belop? = null) =
        JsonOkonomioversiktUtgift()
            // TODO Sjekk om alle utgifter er kilde = BRUKER
            .withKilde(JsonKilde.BRUKER)
            .withType(type.toSoknadJsonTypeString())
            .withTittel(toTittel())
            .withBelop(belop?.belop?.toInt())
            .withOverstyrtAvBruker(false)

    private fun Utgift.toJsonOpplysningUtgifter(): List<JsonOkonomiOpplysningUtgift> {
        return utgiftDetaljer.detaljer.let { detaljer ->
            if (detaljer.isEmpty()) {
                listOf(toJsonOpplysningUtgift())
            } else {
                detaljer.map { detalj -> this.copy().toJsonOpplysningUtgift(detalj as Belop, detalj.beskrivelse) }
            }
        }
    }

    private fun Utgift.toJsonOpplysningUtgift(
        belop: Belop? = null,
        detaljBeskrivelse: String? = null,
    ) =
        JsonOkonomiOpplysningUtgift()
            .withKilde(JsonKilde.BRUKER)
            .withType(type.toSoknadJsonTypeString())
            .withTittel(toTittel(detaljBeskrivelse))
            .withBelop(belop?.belop?.toInt())
            .withOverstyrtAvBruker(false)
}

private fun Utgift.toTittel(detaljBeskrivelse: String? = null): String {
    return when (type) {
        UtgiftType.UTGIFTER_ANNET_BO -> "Annen, bo (brukerangitt):${detaljBeskrivelse ?: ""}"
        UtgiftType.UTGIFTER_ANNET_BARN -> "Annen, barn(brukerangitt):${detaljBeskrivelse ?: ""}"
        UtgiftType.UTGIFTER_BARN_TANNREGULERING -> "Tannregulering for barn (siste regning)"
        UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT -> "Kommunal avgift (siste regning)"
        UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER -> "Fritidsaktiviteter for barn (siste regning):"
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
