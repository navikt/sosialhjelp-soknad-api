package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType

class FormueToJsonMapper(
    private val formuer: Set<Formue>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiElementsToJsonMapper {
    private val oversikt = jsonOkonomi.oversikt
    private val opplysninger = jsonOkonomi.opplysninger

    override fun doMapping() {
        oversikt.formue.addAll(
            formuer.flatMap { it.toJsonFormuer() },
        )
        // TODO JsonOkonomibeskrivelseAvAnnet er merket som "overflødig" i filformatet
        formuer.find { it.type == FormueType.FORMUE_ANNET }?.let { addBeskrivelseSparing(it.beskrivelse) }
        formuer.find { it.type == FormueType.VERDI_ANNET }?.let { addBeskrivelseVerdi(it.beskrivelse) }
    }

    private fun Formue.toJsonFormuer(): List<JsonOkonomioversiktFormue> {
        return formueDetaljer.detaljer.let { detaljer ->
            if (detaljer.isEmpty()) {
                listOf(this.toJsonFormue())
            } else {
                detaljer.map { this.copy().toJsonFormue(it.belop.toInt()) }
            }
        }
    }

    private fun addBeskrivelseSparing(beskrivelse: String?) {
        val jsonBeskrivelser = opplysninger.beskrivelseAvAnnet
        beskrivelse?.let { jsonBeskrivelser.sparing = it }
    }

    private fun addBeskrivelseVerdi(beskrivelse: String?) {
        val jsonBeskrivelser = opplysninger.beskrivelseAvAnnet
        beskrivelse?.let { jsonBeskrivelser.verdi = it }
    }
}

private fun Formue.toJsonFormue(belop: Int? = null) =
    JsonOkonomioversiktFormue()
        .withKilde(JsonKilde.BRUKER)
        .withType(type.toSoknadJsonTypeString())
        .withTittel(toTittel())
        .withBelop(belop)
        .withOverstyrtAvBruker(false)

private fun Formue.toTittel(): String {
    return when (type) {
        FormueType.FORMUE_BRUKSKONTO -> "Brukskonto"
        FormueType.FORMUE_BSU -> "BSU"
        FormueType.FORMUE_LIVSFORSIKRING -> "Livsforsikringssparedel"
        FormueType.FORMUE_SPAREKONTO -> "Sparekonto"
        FormueType.FORMUE_VERDIPAPIRER -> "Aksjer, obligasjoner eller fond"
        FormueType.FORMUE_ANNET -> "Annen form for sparing"
        FormueType.VERDI_BOLIG -> "Bolig"
        FormueType.VERDI_CAMPINGVOGN -> "Campingvogn og/eller båt"
        FormueType.VERDI_KJORETOY -> "Kjøretøy"
        FormueType.VERDI_FRITIDSEIENDOM -> "Fritidseiendom"
        FormueType.VERDI_ANNET -> "Annet"
    }
}

private fun FormueType.toSoknadJsonTypeString(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
        ?: error("Manglende mapping av FormueType for $this")
}
