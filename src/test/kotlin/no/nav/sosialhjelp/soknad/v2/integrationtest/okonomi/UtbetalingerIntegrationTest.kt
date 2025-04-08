package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarIkkeUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.UtbetalingerDto
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class UtbetalingerIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Test
    fun `Hente utbetalinger skal returnere eksisterende data`() {
        val beskrivelse = "Beskrivelse av Utbetaling"
        setOf(
            Inntekt(InntektType.UTBETALING_SALG),
            Inntekt(InntektType.UTBETALING_UTBYTTE),
            Inntekt(InntektType.UTBETALING_ANNET, beskrivelse = beskrivelse),
        )
            .also {
                okonomiRepository.save(
                    opprettOkonomi(soknad.id)
                        .copy(
                            inntekter = it,
                            bekreftelser = setOf(Bekreftelse(BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)),
                        ),
                )
            }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = UtbetalingerDto::class.java,
        )
            .also { dto ->
                assertThat(dto.hasUtbytte).isTrue()
                assertThat(dto.hasSalg).isTrue()
                assertThat(dto.hasAnnenUtbetaling).isTrue()
                assertThat(dto.beskrivelseUtbetaling).isEqualTo(beskrivelse)
            }
    }

    @Test
    fun `Oppdatere input skal lagres i db`() {
        val beskrivelse = "Beskrivelse av Utbetaling"

        val input =
            HarUtbetalingerInput(
                hasUtbytte = true,
                hasSalg = false,
                hasForsikring = false,
                hasAnnet = true,
                beskrivelseUtbetaling = beskrivelse,
            )

        doPut(
            uri = getUrl(soknad.id),
            requestBody = input,
            responseBodyClass = UtbetalingerDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.let { okonomi ->
            assertThat(okonomi.bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_UTBETALING }
            assertThat(okonomi.inntekter.toList()).hasSize(2)
                .anyMatch { it.type == InntektType.UTBETALING_UTBYTTE }
                .anyMatch { it.type == InntektType.UTBETALING_ANNET && it.beskrivelse == beskrivelse }
        }
        dokRepository.findAllBySoknadId(soknad.id).also { dok ->
            assertThat(dok).hasSize(2)
                .anyMatch { it.type == InntektType.UTBETALING_UTBYTTE }
                .anyMatch { it.type == InntektType.UTBETALING_ANNET }
        }
    }

    @Test
    fun `Oppdatere med HarIkkeUtbetalinger skal fjerne alle eksisterende elementer`() {
        okonomiRepository.save(
            opprettOkonomi(soknad.id).copy(
                inntekter = setOf(Inntekt(type = InntektType.UTBETALING_UTBYTTE)),
                bekreftelser = setOf(Bekreftelse(type = BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)),
            ),
        )
        doGet(getUrl(soknad.id), UtbetalingerDto::class.java).also { assertThat(it.hasUtbytte).isTrue() }

        doPut(
            uri = getUrl(soknad.id),
            requestBody = HarIkkeUtbetalingerInput(),
            responseBodyClass = UtbetalingerDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.let { okonomi ->
            assertThat(okonomi.bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_UTBETALING && !it.verdi }
            assertThat(okonomi.inntekter).isEmpty()
        }
        assertThat(dokRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/utbetalinger"
    }
}
