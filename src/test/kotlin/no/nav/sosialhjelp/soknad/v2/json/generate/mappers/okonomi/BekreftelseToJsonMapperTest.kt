package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.BekreftelseToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
                .anyMatch { it.type == BekreftelseType.BEKREFTELSE_SPARING.name }
                .anyMatch { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE.name }
        }
    }
}
