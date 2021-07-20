package no.nav.sosialhjelp.soknad.consumer.unleash;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ByInstanceIdStrategyTest {

    private final ByInstanceIdStrategy strategy = new ByInstanceIdStrategy();

    @AfterEach
    public void tearDown() {
        System.clearProperty("unleash_instance_id");
    }

    @Test
    public void shouldReturnFalse_instanceIdNotInMap() {
        System.setProperty("unleash_instance_id", "local");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    public void shoudReturnTrue_instanceIdInMap() {
        System.setProperty("unleash_instance_id", "dev-sbs");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

}