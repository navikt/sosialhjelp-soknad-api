package no.nav.sosialhjelp.soknad.business.util;

import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.business.util.MetricsUtils.getProsent;
import static org.assertj.core.api.Assertions.assertThat;

class MetricsUtilsTest {

    @Test
    void getProsent_shouldWork() {
        assertThat(getProsent(4, 18)).isEqualTo(22);
        assertThat(getProsent(99, 100)).isEqualTo(99);
    }

    @Test
    void getProsent_shouldRoundToNearestInteger() {
        assertThat(getProsent(3_499, 100_000)).isEqualTo(3);
        assertThat(getProsent(3_500, 100_000)).isEqualTo(4);
        assertThat(getProsent(3_599, 100_000)).isEqualTo(4);
    }

    @Test
    void getProsent_whenDividingByZero_shouldGiveZero() {
        assertThat(getProsent(7, 0)).isZero();
    }
}