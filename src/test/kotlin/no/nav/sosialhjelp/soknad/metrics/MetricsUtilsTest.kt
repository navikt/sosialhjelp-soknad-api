package no.nav.sosialhjelp.soknad.metrics

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MetricsUtilsTest {
    @Test
    fun prosent_shouldWork() {
        assertThat(MetricsUtils.getProsent(4, 18)).isEqualTo(22)
        assertThat(MetricsUtils.getProsent(99, 100)).isEqualTo(99)
    }

    @Test
    fun prosent_shouldRoundToNearestInteger() {
        assertThat(MetricsUtils.getProsent(3499, 100000)).isEqualTo(3)
        assertThat(MetricsUtils.getProsent(3500, 100000)).isEqualTo(4)
        assertThat(MetricsUtils.getProsent(3599, 100000)).isEqualTo(4)
    }

    @Test
    fun prosent_whenDividingByZero_shouldGiveZero() {
        assertThat(MetricsUtils.getProsent(7, 0)).isZero
    }
}
