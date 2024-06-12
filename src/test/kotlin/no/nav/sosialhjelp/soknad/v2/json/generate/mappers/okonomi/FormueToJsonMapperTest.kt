package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sosialhjelp.soknad.v2.createFormuer
import no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi.FormueToJsonMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FormueToJsonMapperTest : AbstractOkonomiMapperTest() {
    @Test
    fun `Formue skal mappes til JsonOkonomioversiktFormue`() {
        val formuer = createFormuer()

        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(formue).hasSize(2)
            formuer.forEach { domain -> assertThat(formue.find { it.type == domain.type.name }).isNotNull }
        }
    }

    @Test
    fun `Formue med flere rader skal generere flere JsonOkonomioversiktFormue-innslag`() {
        val formuer = setOf(Formue(type = FormueType.FORMUE_BRUKSKONTO, formueDetaljer = createOkonomiskeDetaljer()))

        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi.oversikt) {
            assertThat(formue)
                .hasSize(3)
                .allMatch { it.type == formuer.first().type.name }
                .anyMatch { it.belop == 344 }
                .anyMatch { it.belop == 244 }
                .anyMatch { it.belop == 644 }
        }
    }

    @Test
    fun `FormueType ANNET skal lagre beskrivelse i JsonBeskrivelserAvAnnet`() {
        val formuer =
            setOf(
                Formue(
                    type = FormueType.FORMUE_ANNET,
                    beskrivelse = "Beskrivelse av Formue",
                    formueDetaljer = OkonomiskeDetaljer(listOf(Belop(423.0), Belop(288.0))),
                ),
                Formue(
                    type = FormueType.VERDI_ANNET,
                    beskrivelse = "Beskrivelse av Verdi",
                    formueDetaljer = OkonomiskeDetaljer(listOf(Belop(523.0), Belop(121.0))),
                ),
            )
        FormueToJsonMapper(formuer, jsonOkonomi).doMapping()

        with(jsonOkonomi) {
            assertThat(oversikt.formue).hasSize(4).anyMatch { it.belop == 423 }.anyMatch { it.belop == 288 }
                .anyMatch { it.belop == 523 }.anyMatch { it.belop == 121 }
                .allMatch { it.type == FormueType.FORMUE_ANNET.name || it.type == FormueType.VERDI_ANNET.name }

            assertThat(opplysninger.beskrivelseAvAnnet.sparing)
                .isEqualTo(formuer.find { it.type == FormueType.FORMUE_ANNET }?.beskrivelse)

            assertThat(opplysninger.beskrivelseAvAnnet.verdi)
                .isEqualTo(formuer.find { it.type == FormueType.VERDI_ANNET }?.beskrivelse)
        }
    }
}

private fun createOkonomiskeDetaljer(): OkonomiskeDetaljer<Belop> {
    return OkonomiskeDetaljer(
        detaljer =
            listOf(
                Belop(344.0),
                Belop(244.0),
                Belop(644.0),
            ),
    )
}
