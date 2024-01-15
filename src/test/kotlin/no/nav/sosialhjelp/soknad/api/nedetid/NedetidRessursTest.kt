package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService.Companion.dateTimeFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NedetidRessursTest {

    private var nedetidRessurs: NedetidRessurs = NedetidRessurs(NedetidService(null, null))

    // Utenfor planlagt nedetid eller nedetid:
    @Test
    fun whenNedetidIsNull_ShouldReturnNullAndFalse() {
        var nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        val nedetidStart = LocalDateTime.now().format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = null),
        )
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()

        val nedetidSlutt = LocalDateTime.now().format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = null, nedetidSlutt = nedetidSlutt),
        )
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidStart).isNull()
    }

    @Test
    fun whenNedetidStarterOm15dager_ShouldReturnFalseAndNull() {
        val nedetidStart = LocalDateTime.now().plusDays(15).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(20).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidStarterOm14dagerAnd1min_ShouldReturnFalseAndNull() {
        val nedetidStart = LocalDateTime.now().plusDays(14).plusMinutes(1).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(20).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Innenfor planlagt nedetid:
    @Test
    fun whenNedetidStarterOm12dager_ShouldReturnPlanlagtNedetid() {
        val nedetidStart = LocalDateTime.now().plusDays(12).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(20).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidStarterOm14dagerMinus1min_ShouldReturnPlanlagtNedetid() {
        val nedetidStart = LocalDateTime.now().plusDays(14).minusMinutes(1).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(20).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isTrue
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Innenfor nedetid:
    @Test
    fun whenNedetidStartetfor1sekSiden_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusSeconds(1).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(20).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenMidtINedetid_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusDays(5).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusDays(5).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidSlutterOm1min_ShouldReturnNedetid() {
        val nedetidStart = LocalDateTime.now().minusDays(2).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().plusMinutes(1).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isTrue
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isEqualTo(nedetidStart.format(dateTimeFormatter))
        assertThat(nedetidFrontend.nedetidSlutt).isEqualTo(nedetidSlutt.format(dateTimeFormatter))
    }

    // Etter nedetid
    @Test
    fun whenNedetidSluttetFor1sekSiden_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().minusDays(5).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().minusSeconds(1).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenNedetidSluttetFor1ArSiden_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().minusDays(5).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().minusYears(1).plusDays(1).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    // Rare caser
    @Test
    fun whenSluttIsBeforeStart_ShouldReturnFalse() {
        val nedetidStart = LocalDateTime.now().plusDays(2).format(dateTimeFormatter)
        val nedetidSlutt = LocalDateTime.now().minusDays(1).format(dateTimeFormatter)
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = nedetidStart, nedetidSlutt = nedetidSlutt),
        )

        val nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNotNull
        assertThat(nedetidFrontend.nedetidStart).isNotNull
    }

    @Test
    fun whenPropertyIsNotInDateformat_ShouldReturnNullAndFalse() {
        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = "noe", nedetidSlutt = null),
        )
        var nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = null, nedetidSlutt = "null"),
        )
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()

        nedetidRessurs = NedetidRessurs(
            NedetidService(nedetidStart = "", nedetidSlutt = "noe"),
        )
        nedetidFrontend = nedetidRessurs.hentNedetidInformasjon()
        assertThat(nedetidFrontend.isNedetid).isFalse
        assertThat(nedetidFrontend.isPlanlagtNedetid).isFalse
        assertThat(nedetidFrontend.nedetidSlutt).isNull()
        assertThat(nedetidFrontend.nedetidStart).isNull()
    }
}
