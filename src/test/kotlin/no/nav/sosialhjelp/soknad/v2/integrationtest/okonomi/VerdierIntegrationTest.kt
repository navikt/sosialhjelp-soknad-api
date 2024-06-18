package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarIkkeVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.HarVerdierInput
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.VerdierDto
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class VerdierIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Test
    fun `Hente verdier skal returnere lagrede data`() {
        opprettOkonomi(soknad.id).copy(
            bekreftelser = setOf(Bekreftelse(BekreftelseType.BEKREFTELSE_VERDI, verdi = true)),
            formuer =
                setOf(
                    Formue(FormueType.VERDI_BOLIG),
                    Formue(FormueType.VERDI_ANNET, beskrivelse = "beskrivelse"),
                ),
        ).also { okonomiRepository.save(it) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = VerdierDto::class.java,
        )
            .also { dto ->
                assertThat(dto.hasBolig).isTrue()
                assertThat(dto.hasAnnet).isTrue()
                assertThat(dto.beskrivelseVerdi).isEqualTo("beskrivelse")
            }
    }

    @Test
    fun `Oppdatere verdier skal lagres i db`() {
        doPut(
            uri = getUrl(soknad.id),
            requestBody = HarVerdierInput(hasBeskrivelseAnnet = true, beskrivelseVerdi = "beskrivelse"),
            responseBodyClass = VerdierDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.bekreftelser.toList()).hasSize(1)
                .allMatch { it.type == BekreftelseType.BEKREFTELSE_VERDI && it.verdi }
            assertThat(okonomi.formuer.toList()).hasSize(1)
                .allMatch { it.type == FormueType.VERDI_ANNET && it.beskrivelse == "beskrivelse" }
        }
        // har ikke dokumentasjonskrav
        assertThat(dokRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    @Test
    fun `Sette bekreftelse false skal fjerne elementer`() {
        opprettOkonomi(soknad.id).copy(
            bekreftelser = setOf(Bekreftelse(BekreftelseType.BEKREFTELSE_VERDI, verdi = true)),
            formuer = setOf(Formue(FormueType.VERDI_BOLIG)),
        ).also { okonomiRepository.save(it) }

        doGet(getUrl(soknad.id), VerdierDto::class.java).also { assertThat(it.hasBolig).isTrue() }

        doPut(
            uri = getUrl(soknad.id),
            requestBody = HarIkkeVerdierInput(),
            responseBodyClass = VerdierDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.bekreftelser.toList()).hasSize(1)
                .allMatch { it.type == BekreftelseType.BEKREFTELSE_VERDI && !it.verdi }
            assertThat(okonomi.formuer).isEmpty()
        }
    }

    companion object {
        private fun getUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/verdier"
    }
}
