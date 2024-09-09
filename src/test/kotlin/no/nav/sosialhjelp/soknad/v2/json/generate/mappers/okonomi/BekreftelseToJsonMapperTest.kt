package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.BekreftelseToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.toTittel
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BekreftelseToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Liste med Bekreftelser skal opprette tilsvarende antall innslag i Json-strukturen`() {
        val bekreftelser =
            setOf(
                Bekreftelse(type = BekreftelseType.BEKREFTELSE_SPARING, verdi = true),
                Bekreftelse(type = BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = false),
            )
        BekreftelseToJsonMapper(bekreftelser, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(bekreftelse).hasSize(2)

            bekreftelse.find { it.type == BekreftelseType.BEKREFTELSE_SPARING.name }!!.let {
                assertThat(it.verdi).isEqualTo(true)
                assertThat(it.bekreftelsesDato).isEqualTo(LocalDate.now().toString())
                assertThat(it.tittel).isEqualTo(BekreftelseType.BEKREFTELSE_SPARING.toTittel())
                assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
            }
            bekreftelse.find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE.name }!!.let {
                assertThat(it.verdi).isEqualTo(false)
                assertThat(it.bekreftelsesDato).isEqualTo(LocalDate.now().toString())
                assertThat(it.tittel).isEqualTo(BekreftelseType.BOSTOTTE_SAMTYKKE.toTittel())
                assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
            }
        }
    }
}
