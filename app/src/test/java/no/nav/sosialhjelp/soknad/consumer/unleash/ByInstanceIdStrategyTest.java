package no.nav.sosialhjelp.soknad.consumer.unleash;

import org.junit.After;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByInstanceIdStrategyTest {

    private final ByInstanceIdStrategy strategy = new ByInstanceIdStrategy();

    @After
    public void tearDown() {
        System.clearProperty("unleash_instance_id");
    }

    @Test
    public void shouldReturnFalse_instanceIdNotInMap() {
        System.setProperty("unleash_instance_id", "local");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertFalse(strategy.isEnabled(parameters));
    }

    @Test
    public void shoudReturnTrue_instanceIdInMap() {
        System.setProperty("unleash_instance_id", "dev-sbs");
        var parameters = Map.of("instance.id", "dev-sbs,dev-sbs-intern");

        assertTrue(strategy.isEnabled(parameters));
    }

}