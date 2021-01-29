package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.junit.Test;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.MetricsUtils.getProsent;
import static org.junit.Assert.*;

public class MetricsUtilsTest {

    @Test
    public void getProsent_shouldWork() {
        assertEquals(38, getProsent(7, 18), 0);
        assertEquals(99, getProsent(99, 100), 0);
    }

    @Test
    public void getProsent_whenDividingByZero_shouldGiveZero() {
        assertEquals(0, getProsent(7, 0), 0);
    }
}