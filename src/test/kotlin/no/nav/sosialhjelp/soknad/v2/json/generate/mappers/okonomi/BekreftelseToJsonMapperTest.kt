package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.BekreftelseToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.toTittel
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.shadow.okonomi.SoknadJsonTypeEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BekreftelseToJsonMapperTest : AbstractOkonomiMapperTest() {
    private val timestampRegex = "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9].[0-9][0-9]*Z\$"

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

            bekreftelse.find { it.type == SoknadJsonTypeEnum.BEKREFTELSE_SPARING.verdi }!!.let {
                assertThat(it.verdi).isEqualTo(true)
                assertThat(it.bekreftelsesDato).matches(timestampRegex)
                assertThat(it.tittel).isEqualTo(BekreftelseType.BEKREFTELSE_SPARING.toTittel())
                assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
            }
            bekreftelse.find { it.type == SoknadJsonTypeEnum.BOSTOTTE_SAMTYKKE.verdi }!!.let {
                assertThat(it.verdi).isEqualTo(false)
                assertThat(it.bekreftelsesDato).matches(timestampRegex)
                assertThat(it.tittel).isEqualTo(BekreftelseType.BOSTOTTE_SAMTYKKE.toTittel())
                assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
            }
        }
    }
}
