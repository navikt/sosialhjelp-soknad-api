package no.nav.sosialhjelp.soknad.api

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TimeUtils {
    fun toUtc(localDateTime: LocalDateTime, zoneId: ZoneId): LocalDateTime {
        return ZonedDateTime.of(localDateTime, zoneId)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime()
    }
}
