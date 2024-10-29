package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.toTittel
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import org.junit.jupiter.api.Test

class BekreftelseComparatorTest {
    @Test
    fun testBekreftelse() {
        BekreftelseComparator(
            createOriginalJsonBekreftelser(),
            createShadowBekreftelser(),
        ).compare()

        val a = 4
    }
}

fun createOriginalJsonBekreftelser(): List<JsonOkonomibekreftelse> {
    return listOf(
        JsonOkonomibekreftelse()
            .withKilde(JsonKilde.BRUKER)
            .withType(OpplysningTypeMapper.getJsonVerdier(BekreftelseType.BEKREFTELSE_SPARING).navn?.verdi)
            .withTittel(BekreftelseType.BEKREFTELSE_SPARING.toTittel())
            .withVerdi(false)
            .withBekreftelsesDato("2021-01-01T00:00:00Z"),
    )
}

fun createShadowBekreftelser(): List<JsonOkonomibekreftelse> {
    return listOf(
        JsonOkonomibekreftelse()
            .withKilde(JsonKilde.BRUKER)
            .withType(OpplysningTypeMapper.getJsonVerdier(BekreftelseType.BEKREFTELSE_SPARING).navn?.verdi)
            .withTittel(BekreftelseType.BEKREFTELSE_SPARING.toTittel())
            .withVerdi(true)
            .withBekreftelsesDato("2023-01-01T00:00:00Z"),
    )
}
