package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class InnsendtSoknadServiceTest {

    @Test
    public void soknadsalderIMinutter_returnsMinutes() {
        LocalDateTime tidspunktSendt = LocalDateTime.now().minusDays(1).plusHours(2).minusMinutes(3); // (24-2)h * 60 m/h - 3 = 22*60-3 =
        long response = InnsendtSoknadService.soknadsalderIMinutter(tidspunktSendt);

        assertEquals(1323, response);// (24-2)h * 60 m/h + 3 = 22*60+3
    }

    @Test
    public void soknadsalderIMinutter_whenDateTimeIsNull_returnsMinusOne() {
        long response = InnsendtSoknadService.soknadsalderIMinutter(null);

        assertEquals(-1, response);
    }
}