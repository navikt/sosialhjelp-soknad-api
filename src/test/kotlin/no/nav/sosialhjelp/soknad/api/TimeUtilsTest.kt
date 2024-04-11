package no.nav.sosialhjelp.soknad.api

import no.nav.sosialhjelp.soknad.api.TimeUtils.toUtc
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

internal class TimeUtilsTest {
    @Test
    fun skalKonvertereTilUtc() {
        val summerTime = LocalDateTime.of(2021, 6, 6, 12, 12, 12)
        val winterTime = LocalDateTime.of(2021, 1, 1, 12, 12, 12)
        assertThat(toUtc(summerTime, ZoneId.of("Europe/Oslo"))).isEqualTo(summerTime.minusHours(2))
        assertThat(toUtc(winterTime, ZoneId.of("Europe/Oslo"))).isEqualTo(winterTime.minusHours(1))
        assertThat(toUtc(winterTime, ZoneId.of("America/Chicago"))).isEqualTo(winterTime.plusHours(6))
    }
}
