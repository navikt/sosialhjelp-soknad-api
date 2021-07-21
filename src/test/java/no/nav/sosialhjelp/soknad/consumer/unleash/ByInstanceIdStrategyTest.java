package no.nav.sosialhjelp.soknad.consumer.unleash;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ByInstanceIdStrategyTest {

    private ByInstanceIdStrategy strategy;

    @Test
    void shouldReturnFalse_instanceIdNotInMap() {
        strategy = new ByInstanceIdStrategy("local");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    void shoudReturnTrue_instanceIdInMap() {
        strategy = new ByInstanceIdStrategy("dev-sbs");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

}