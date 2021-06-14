package no.nav.sosialhjelp.soknad.business.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeUtilsTest {

    @Test
    public void skalKonvertereTilUtc() {
        var summerTime = LocalDateTime.of(2021, 6, 6, 12, 12, 12);
        var winterTime = LocalDateTime.of(2021, 1, 1, 12, 12, 12);

        assertThat(TimeUtils.toUtc(summerTime, ZoneId.of("Europe/Oslo"))).isEqualTo(summerTime.minusHours(2));
        assertThat(TimeUtils.toUtc(winterTime, ZoneId.of("Europe/Oslo"))).isEqualTo(winterTime.minusHours(1));

        assertThat(TimeUtils.toUtc(winterTime, ZoneId.of("America/Chicago"))).isEqualTo(winterTime.plusHours(6));
    }
}