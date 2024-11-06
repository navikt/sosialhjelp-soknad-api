package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.NavYtelseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class NavYtelseIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Test
    fun `Hente navYtelse skal returnerer lagrede data`() {
        integrasjonStatusService.setUtbetalingerFraNavStatus(soknad.id, feilet = false)
        val inntekt = createNavYtelse().also { okonomiService.addElementToOkonomi(soknad.id, it) }

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = NavYtelseDto::class.java,
        )
            .also {
                assertThat(it.utbetalinger.size).isEqualTo(inntekt.inntektDetaljer.detaljer.size)
                assertThat(it.utbetalinger).allMatch { it.type == InntektType.UTBETALING_NAVYTELSE }
            }
    }

    @Test
    fun `Fetch feilet skal returnere true i dto`() {
        integrasjonStatusService.setUtbetalingerFraNavStatus(soknad.id, feilet = true)

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = NavYtelseDto::class.java,
        )
            .also { assertThat(it.fetchUtbetalingerFeilet).isTrue() }
    }

    @Test
    fun `Ingen utbetalinger skal returnere tom liste`() {
        integrasjonStatusService.setUtbetalingerFraNavStatus(soknad.id, feilet = false)

        doGet(
            uri = getUrl(soknad.id),
            responseBodyClass = NavYtelseDto::class.java,
        )
            .also { assertThat(it.utbetalinger).isEmpty() }
    }

    companion object {
        private fun getUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/navytelse"
    }
}

private fun createNavYtelse(): Inntekt {
    return Inntekt(
        type = InntektType.UTBETALING_NAVYTELSE,
        inntektDetaljer =
            OkonomiDetaljer(
                detaljer =
                    listOf(
                        UtbetalingMedKomponent(
                            tittel = "NAV Ytelse",
                            utbetaling =
                                Utbetaling(
                                    utbetalingsdato = LocalDate.now().minusDays(2),
                                    belop = 1000.0,
                                ),
                        ),
                        UtbetalingMedKomponent(
                            tittel = "NAV Ytelse 2",
                            utbetaling =
                                Utbetaling(
                                    utbetalingsdato = LocalDate.now().minusDays(1),
                                    belop = 2000.0,
                                ),
                        ),
                    ),
            ),
    )
}
