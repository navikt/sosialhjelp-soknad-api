package no.nav.sosialhjelp.soknad.api.nedetid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NedetidRessursTest {

    var nedetidRessurs = NedetidRessurs()

    @BeforeEach
    fun setUp() {
        System.clearProperty(NedetidUtils.NEDETID_START)
        System.clearProperty(NedetidUtils.NEDETID_SLUTT)
    }

    // Utenfor planlagt nedetid eller nedetid:
    @Test
    fun whenNedetidIsNull_ShouldReturnNullAndFalse() {
        var nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        System.setProperty(NedetidUtils.NEDETID_START, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter))
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()

        System.clearProperty(NedetidUtils.NEDETID_START)
        System.setProperty(NedetidUtils.NEDETID_SLUTT, LocalDateTime.now().format(NedetidUtils.dateTimeFormatter))
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidStart).isNull()
    }

    @Test
    fun whenNedetidStarterOm15dager_ShouldReturnFalseAndNull() {
        val nedetidStart = LocalDateTime.now().plusDays(15)
        val nedetidSlutt = LocalDateTime.now().plusDays(20)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidStarterOm14dagerAnd1min_ShouldReturnFalseAndNull() {
        val nedetidStart = LocalDateTime.now().plusDays(14).plusMinutes(1)
        val nedetidSlutt = LocalDateTime.now().plusDays(20)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Innenfor planlagt nedetid:
    @Test
    fun whenNedetidStarterOm12dager_ShouldReturnPlanlagtNedetid() {
        val nedetidStart = LocalDateTime.now().plusDays(12)
        val nedetidSlutt = LocalDateTime.now().plusDays(20)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidStarterOm14dagerMinus1min_ShouldReturnPlanlagtNedetid() {
        val nedetidStart = LocalDateTime.now().plusDays(14).minusMinutes(1)
        val nedetidSlutt = LocalDateTime.now().plusDays(20)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Innenfor nedetid:
    @Test
    fun whenNedetidStartetfor1sekSiden_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusSeconds(1)
        val nedetidSlutt = LocalDateTime.now().plusDays(20)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenMidtINedetid_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusDays(5)
        val nedetidSlutt = LocalDateTime.now().plusDays(5)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidSlutterOm1min_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusDays(2)
        val nedetidSlutt = LocalDateTime.now().plusMinutes(1)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isEqualTo(nedetidStart.format(NedetidUtils.dateTimeFormatter))
        assertThat(nedetidFrontend.nedetidSlutt).isEqualTo(nedetidSlutt.format(NedetidUtils.dateTimeFormatter))
    }

    // Etter nedetid
    @Test
    fun whenNedetidSluttetFor1sekSiden_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().minusDays(5)
        val nedetidSlutt = LocalDateTime.now().minusSeconds(1)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidSluttetFor1ArSiden_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().minusDays(5)
        val nedetidSlutt = LocalDateTime.now().minusYears(1).plusDays(1)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Rare caser
    @Test
    fun whenSluttIsBeforeStart_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().plusDays(2)
        val nedetidSlutt = LocalDateTime.now().minusDays(1)
        System.setProperty(NedetidUtils.NEDETID_START, nedetidStart.format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NedetidUtils.NEDETID_SLUTT, nedetidSlutt.format(NedetidUtils.dateTimeFormatter))

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenPropertyIsNotInDateformat_ShouldReturnNullAndFalse() {
        System.setProperty(NedetidUtils.NEDETID_START, "noe")
        var nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        System.clearProperty(NedetidUtils.NEDETID_START)
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "null")
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        System.setProperty(NedetidUtils.NEDETID_START, "")
        System.setProperty(NedetidUtils.NEDETID_SLUTT, "noe")
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()
    }
}
