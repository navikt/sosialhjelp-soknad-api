package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType

class BekreftelseToJsonMapper(
    private val bekreftelser: Set<Bekreftelse>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiElementsToJsonMapper {
    private val opplysninger = jsonOkonomi.opplysninger

    override fun doMapping() {
        opplysninger.bekreftelse.addAll(bekreftelser.map { it.toJsonBekreftelse() })
        addBostotteSamtykkeFalseIfBostotteIsFalse(opplysninger)
    }

    // "Gammel modell" legger til samtykke uavhengig av om eksisterende bostotte er true eller false
    private fun addBostotteSamtykkeFalseIfBostotteIsFalse(opplysninger: JsonOkonomiopplysninger) {
        bekreftelser.find { it.type == BekreftelseType.BOSTOTTE }
            ?.takeIf { !it.verdi }
            ?.also { bostotte ->
                opplysninger.bekreftelse.add(
                    JsonOkonomibekreftelse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(BekreftelseType.BOSTOTTE_SAMTYKKE.toSoknadJsonTypeString())
                        .withTittel(BekreftelseType.BOSTOTTE_SAMTYKKE.toTittel())
                        .withBekreftelsesDato(TimestampConverter.convertToOffsettDateTimeUTCString(bostotte.tidspunkt))
                        .withVerdi(false),
                )
            }
    }
}

private fun Bekreftelse.toJsonBekreftelse(): JsonOkonomibekreftelse {
    return JsonOkonomibekreftelse()
        .withKilde(JsonKilde.BRUKER)
        .withType(type.toSoknadJsonTypeString())
        .withVerdi(verdi)
        .withTittel(type.toTittel())
        .withBekreftelsesDato(TimestampConverter.convertToOffsettDateTimeUTCString(tidspunkt))
}

internal fun BekreftelseType.toTittel(): String {
    return when (this) {
        BekreftelseType.BEKREFTELSE_BARNEUTGIFTER -> "Utgifter til barn"
        BekreftelseType.BEKREFTELSE_BOUTGIFTER -> "Boutgifter"
        BekreftelseType.BEKREFTELSE_SPARING -> "Bankinnskudd eller annen sparing."
        BekreftelseType.BEKREFTELSE_UTBETALING -> "Annen utbetaling"
        BekreftelseType.BEKREFTELSE_VERDI -> "Eier noe av økonomisk verdi."
        BekreftelseType.BOSTOTTE -> "Søkt eller mottatt bostøtte fra Husbanken."
        BekreftelseType.BOSTOTTE_SAMTYKKE -> "Har gitt samtykke til innhenting av opplysninger om bostøtte fra Husbanken."
        BekreftelseType.STUDIELAN_BEKREFTELSE -> "Mottar lån/stipend fra Lånekassen."
        BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE -> "Har gitt samtykke til innhenting av inntektsopplysninger fra Skatteetaten."
    }
}

private fun BekreftelseType.toSoknadJsonTypeString(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi
        ?: error("Manglende BekreftelsesType-mapping for $this")
}
