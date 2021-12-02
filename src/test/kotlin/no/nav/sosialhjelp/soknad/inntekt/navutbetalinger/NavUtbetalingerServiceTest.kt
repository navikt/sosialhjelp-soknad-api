package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.KomponentDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class NavUtbetalingerServiceTest {

    private val navUtbetalingerClient: NavUtbetalingerClient = mockk()
    private val navUtbetalingerService = NavUtbetalingerService(navUtbetalingerClient)

    @Test
    internal fun clientReturnererUtbetalinger() {
        val utbetaling = NavUtbetalingDto(
            "navytelse",
            1000.0,
            1234.0,
            200.0,
            34.0,
            "bilagsnummer",
            LocalDate.now().minusDays(2),
            LocalDate.now().minusDays(14),
            LocalDate.now().minusDays(2),
            listOf(KomponentDto("type", 42.0, "sats", 21.0, 2.0)),
            "tittel",
            "orgnr"
        )

        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns NavUtbetalingerDto(listOf(utbetaling), true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).hasSize(1)
        val navUtbetaling = navUtbetalinger!![0]
        assertThat(navUtbetaling.type).isEqualTo("navytelse")
        assertThat(navUtbetaling.netto).isEqualTo(1000.0)
        assertThat(navUtbetaling.brutto).isEqualTo(1234.0)
        assertThat(navUtbetaling.skattetrekk).isEqualTo(200.0)
        assertThat(navUtbetaling.andreTrekk).isEqualTo(34.0)
        assertThat(navUtbetaling.bilagsnummer).isEqualTo("bilagsnummer")
        assertThat(navUtbetaling.utbetalingsdato).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.periodeFom).isEqualTo(LocalDate.now().minusDays(14))
        assertThat(navUtbetaling.periodeTom).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.komponenter).hasSize(1)
        assertThat(navUtbetaling.komponenter[0].type).isEqualTo("type")
        assertThat(navUtbetaling.komponenter[0].belop).isEqualTo(42.0)
        assertThat(navUtbetaling.komponenter[0].satsType).isEqualTo("sats")
        assertThat(navUtbetaling.komponenter[0].satsBelop).isEqualTo(21.0)
        assertThat(navUtbetaling.komponenter[0].satsAntall).isEqualTo(2.0)
        assertThat(navUtbetaling.tittel).isEqualTo("tittel")
        assertThat(navUtbetaling.orgnummer).isEqualTo("orgnr")
    }

    @Test
    internal fun clientReturnererUtbetalingerUtenKomponenter() {
        val utbetaling = NavUtbetalingDto(
            "navytelse",
            1000.0,
            1234.0,
            200.0,
            34.0,
            "bilagsnummer",
            LocalDate.now().minusDays(2),
            LocalDate.now().minusDays(14),
            LocalDate.now().minusDays(2),
            emptyList(),
            "tittel",
            "orgnr"
        )

        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns NavUtbetalingerDto(listOf(utbetaling), true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).hasSize(1)
        val navUtbetaling = navUtbetalinger!![0]
        assertThat(navUtbetaling.type).isEqualTo("navytelse")
        assertThat(navUtbetaling.netto).isEqualTo(1000.0)
        assertThat(navUtbetaling.brutto).isEqualTo(1234.0)
        assertThat(navUtbetaling.skattetrekk).isEqualTo(200.0)
        assertThat(navUtbetaling.andreTrekk).isEqualTo(34.0)
        assertThat(navUtbetaling.bilagsnummer).isEqualTo("bilagsnummer")
        assertThat(navUtbetaling.utbetalingsdato).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.periodeFom).isEqualTo(LocalDate.now().minusDays(14))
        assertThat(navUtbetaling.periodeTom).isEqualTo(LocalDate.now().minusDays(2))
        assertThat(navUtbetaling.komponenter).hasSize(0)
        assertThat(navUtbetaling.tittel).isEqualTo("tittel")
        assertThat(navUtbetaling.orgnummer).isEqualTo("orgnr")
    }

    @Test
    internal fun clientReturnererTomListe() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns NavUtbetalingerDto(emptyList(), true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }

    @Test
    internal fun clientReturnererNull() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns null

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }

    @Test
    internal fun clientReturnererResponseMedFeiletTrue() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns NavUtbetalingerDto(null, true)

        val navUtbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager("ident")

        assertThat(navUtbetalinger).isNull()
    }
}
