package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.UtgiftToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UtgiftToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Utgift med type SFO skal lage JsonOkonomioversiktUtgift`() {
        val utgifter = setOf(Utgift(UtgiftType.UTGIFTER_SFO))

        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(utgift).hasSize(1).allMatch { it.type == UtgiftType.UTGIFTER_SFO.name }
        }
    }

    @Test
    fun `Utgift med type STROM skal lage JsonOkonomiopplysningUtgift`() {
        val utgifter = setOf(Utgift(UtgiftType.UTGIFTER_STROM))

        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utgift).hasSize(1).allMatch { it.type == UtgiftType.UTGIFTER_STROM.name }
        }
    }

    @Test
    fun `Utgift med flere okonomiske detaljer skal gi flere innslag`() {
        val utgifter =
            setOf(
                Utgift(
                    UtgiftType.BARNEBIDRAG_BETALER,
                    null,
                    OkonomiskeDetaljer(listOf(Belop(444.0), Belop(1242.0))),
                ),
            )
        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(utgift).hasSize(2).allMatch { it.type == UtgiftType.BARNEBIDRAG_BETALER.name }
        }
    }

    @Test
    fun `Beskrivelse for Annen Bosituasjon eller Annen utgift barn skal gi beskrivelse i tittel`() {
        val utgifter =
            setOf(
                Utgift(UtgiftType.UTGIFTER_ANNET_BO, "Beskrivelse av Bo"),
                Utgift(UtgiftType.UTGIFTER_ANNET_BARN, "Beskrivelse av annet Barn"),
            )
        UtgiftToJsonMapper(utgifter, jsonOkonomi).doMapping()

        with(jsonOkonomi.opplysninger) {
            assertThat(utgift).hasSize(2)

            val beskrivelser = utgifter.map { it.beskrivelse }
            beskrivelser.forEach { beskrivelse ->
                assertThat(utgift).anyMatch { it.tittel.contains(beskrivelse!!) }
            }
        }
    }
}
