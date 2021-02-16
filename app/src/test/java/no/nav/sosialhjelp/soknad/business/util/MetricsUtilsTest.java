package no.nav.sosialhjelp.soknad.business.util;

import org.junit.Test;

import static no.nav.sosialhjelp.soknad.business.util.MetricsUtils.getProsent;
import static org.junit.Assert.assertEquals;

public class MetricsUtilsTest {

    @Test
    public void getProsent_shouldWork() {
        assertEquals(22, getProsent(4, 18), 0);
        assertEquals(99, getProsent(99, 100), 0);
    }

    @Test
    public void getProsent_shouldRoundToNearestInteger() {
        assertEquals(3, getProsent(3_499, 100_000), 0);
        assertEquals(4, getProsent(3_500, 100_000), 0);
        assertEquals(4, getProsent(3_599, 100_000), 0);
    }

    @Test
    public void getProsent_whenDividingByZero_shouldGiveZero() {
        assertEquals(0, getProsent(7, 0), 0);
    }
}