package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType

class BekreftelseToJsonMapper(
    private val bekreftelser: Set<Bekreftelse>,
) : OkonomiElementsToJsonMapper {
    override fun doMapping(jsonOkonomi: JsonOkonomi): JsonOkonomi =
        jsonOkonomi.copy(
            opplysninger =
                jsonOkonomi.opplysninger.copy(
                    bekreftelse = jsonOkonomi.opplysninger.bekreftelse + bekreftelser.map { it.toJsonBekreftelse() } + bostotteSamtykke(),
                ),
        )

    // "Gammel modell" legger til samtykke uavhengig av om eksisterende bostotte er true eller false
    private fun bostotteSamtykke(): List<JsonOkonomibekreftelse> =
        bekreftelser.find { it.type == BekreftelseType.BOSTOTTE }
            ?.takeIf { !it.verdi }
            ?.let { bostotte ->
                listOf(
                    JsonOkonomibekreftelse(
                        kilde = JsonKilde.BRUKER,
                        type = BekreftelseType.BOSTOTTE_SAMTYKKE.toSoknadJsonTypeString(),
                        tittel = BekreftelseType.BOSTOTTE_SAMTYKKE.toTittel(),
                        verdi = false,
                        bekreftelsesDato = TimestampUtil.convertToOffsettDateTimeUTCString(bostotte.tidspunkt),
                    ),
                )
            }
            .orEmpty()
}

private fun Bekreftelse.toJsonBekreftelse(): JsonOkonomibekreftelse {
    return JsonOkonomibekreftelse(
        kilde = JsonKilde.BRUKER,
        type = type.toSoknadJsonTypeString(),
        tittel = type.toTittel(),
        verdi = verdi,
        bekreftelsesDato = TimestampUtil.convertToOffsettDateTimeUTCString(tidspunkt),
    )
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
