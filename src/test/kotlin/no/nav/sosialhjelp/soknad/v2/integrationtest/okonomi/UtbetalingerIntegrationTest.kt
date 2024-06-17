package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarIkkeUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.HarUtbetalingerInput
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.UtbetalingerDto
import no.nav.sosialhjelp.soknad.v2.opprettOkonomi
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class UtbetalingerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var okonomiRepository: OkonomiRepository

    @Test
    fun `Hente utbetalinger skal returnere eksisterende data`() {
        val soknad = soknadRepository.save(opprettSoknad())
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
        val soknad = soknadRepository.save(opprettSoknad())
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
    }

    @Test
    fun `Oppdatere med HarIkkeUtbetalinger skal fjerne alle eksisterende elementer`() {
        val soknad = soknadRepository.save(opprettSoknad())
        okonomiRepository.save(
            opprettOkonomi(soknad.id).copy(
                inntekter = setOf(Inntekt(type = InntektType.UTBETALING_UTBYTTE)),
                bekreftelser = setOf(Bekreftelse(type = BekreftelseType.BEKREFTELSE_UTBETALING, verdi = true)),
            ),
        )
        doGet(getUrl(soknad.id), UtbetalingerDto::class.java).also { assertThat(it.hasUtbytte).isTrue() }

        doPut(
            uri = getUrl(soknad.id),
            requestBody = HarIkkeUtbetalingerInput(bekreftelse = false),
            responseBodyClass = UtbetalingerDto::class.java,
            soknadId = soknad.id,
        )

        okonomiRepository.findByIdOrNull(soknad.id)!!.let { okonomi ->
            assertThat(okonomi.bekreftelser).anyMatch { it.type == BekreftelseType.BEKREFTELSE_UTBETALING && !it.verdi }
            assertThat(okonomi.inntekter).isEmpty()
        }
    }

    companion object {
        fun getUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/utbetalinger"
    }
}
