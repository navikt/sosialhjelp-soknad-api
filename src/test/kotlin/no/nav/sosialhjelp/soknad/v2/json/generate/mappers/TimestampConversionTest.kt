package no.nav.sosialhjelp.soknad.v2.json.generate.mappers

import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TimestampConversionTest {
    @Test
    fun `Legg til ett millisekund hvis nanosekund er tilsvarende mindre`() {
        val timeUtenNano = LocalDateTime.of(2020, 5, 5, 5, 5, 5)
        val timeLowNano = timeUtenNano.plusNanos(5)

        val stringUtenNano = TimestampConverter.convertToOffsettDateTimeUTCString(timeUtenNano)
        val stringLowNano = TimestampConverter.convertToOffsettDateTimeUTCString(timeLowNano)

        assertThat(stringUtenNano).isEqualTo(stringLowNano)
    }

    @Test
    fun `Timestamp sommertid skal skille 2 timer`() {
        val timestamp = LocalDateTime.of(2024, 6, 6, 6, 6, 6)

        TimestampConverter.convertToOffsettDateTimeUTCString(timestamp)
            .also { assertThat(it).contains("0${timestamp.hour - 2}:0${timestamp.minute}:0${timestamp.second}") }
    }

    @Test
    fun `Timestamp vintertid skal skille 1 time`() {
        val timestamp = LocalDateTime.of(2024, 12, 12, 6, 6, 6)

        TimestampConverter.convertToOffsettDateTimeUTCString(timestamp)
            .also { assertThat(it).contains("0${timestamp.hour - 1}:0${timestamp.minute}:0${timestamp.second}") }
    }

    @Test
    fun `Konverter tilbake til LocalDateTime`() {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

        val timestampString = TimestampConverter.convertToOffsettDateTimeUTCString(now)
        val parsedLocalDateTime = TimestampConverter.parseFromUTCString(timestampString)

        assertThat(now).isEqualTo(parsedLocalDateTime)
    }

    @Test
    fun `Konverter instant til LocalDateTime`() {
        val longNow = Instant.now().toEpochMilli()
        val localDateTimeNow = LocalDateTime.now()

        TimestampConverter.convertInstantToLocalDateTime(Instant.ofEpochMilli(longNow))
            .also {
                assertThat(it.hour).isEqualTo(localDateTimeNow.hour)
            }
    }
}
