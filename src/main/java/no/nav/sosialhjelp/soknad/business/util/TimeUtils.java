package no.nav.sosialhjelp.soknad.business.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static LocalDateTime toUtc(LocalDateTime localDateTime, ZoneId zoneId) {
        return ZonedDateTime.of(localDateTime, zoneId)
                .withZoneSameInstant(UTC)
                .toLocalDateTime();
    }
}
