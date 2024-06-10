package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.createBostotteSaker
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.BostotteSakToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BostotteSakToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Bostottesaker skal mappes til tilsvarende innslag i JsonBostotteSaker`() {
        val bostotteSaker = createBostotteSaker()

        BostotteSakToJsonMapper(bostotteSaker, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(bostotte).isNotNull
            assertThat(bostotte.saker).hasSize(2)
                .allMatch { it.type == InntektType.UTBETALING_HUSBANKEN.name }
                .anyMatch { it.status == BostotteStatus.VEDTATT.name }
                .anyMatch { it.status == BostotteStatus.UNDER_BEHANDLING.name }
        }
    }
}
