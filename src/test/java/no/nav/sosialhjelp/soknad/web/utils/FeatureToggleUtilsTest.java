package no.nav.sosialhjelp.soknad.web.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureToggleUtilsTest {

    @Test
    void enableModalV2Test() {
        assertThat(FeatureToggleUtils.enableModalV2("1234")).isTrue();
        assertThat(FeatureToggleUtils.enableModalV2("1235")).isFalse();
        assertThat(FeatureToggleUtils.enableModalV2("asdf")).isFalse();
        assertThat(FeatureToggleUtils.enableModalV2(null)).isFalse();
        assertThat(FeatureToggleUtils.enableModalV2("01234")).isTrue();
    }
}