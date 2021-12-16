package no.nav.sosialhjelp.soknad.api.featuretoggle

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class FeatureToggleUtilsTest {

    @Test
    fun enableModalV2Test() {
        Assertions.assertThat(FeatureToggleUtils.enableModalV2("1234")).isTrue
        Assertions.assertThat(FeatureToggleUtils.enableModalV2("1235")).isFalse
        Assertions.assertThat(FeatureToggleUtils.enableModalV2("asdf")).isFalse
        Assertions.assertThat(FeatureToggleUtils.enableModalV2(null)).isFalse
        Assertions.assertThat(FeatureToggleUtils.enableModalV2("01234")).isTrue
    }
}
