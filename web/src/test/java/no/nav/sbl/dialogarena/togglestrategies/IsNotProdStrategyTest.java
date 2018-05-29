package no.nav.sbl.dialogarena.togglestrategies;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static no.nav.sbl.dialogarena.togglestrategies.IsNotProdStrategy.ENVIRONMENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;


public class IsNotProdStrategyTest {

    private IsNotProdStrategy isNotProdStrategy;

    @Before
    public void setUp() throws Exception {
        isNotProdStrategy = new IsNotProdStrategy();
    }

    @Test
    public void isNotEnabledInP() {
        assertThat(isNotProdStrategy.isEnabled(parameterForEnvironment("p"))).isFalse();
    }

    @Test
    public void isNotEnabledInQ0() {
        assertThat(isNotProdStrategy.isEnabled(parameterForEnvironment("q0"))).isFalse();
    }

    @Test
    public void isEnabledInQ() {
        assertThat(isNotProdStrategy.isEnabled(parameterForEnvironment("q6"))).isTrue();
    }

    @Test
    public void isEnabledInT() {
        assertThat(isNotProdStrategy.isEnabled(parameterForEnvironment("t6"))).isTrue();
    }

    @Test
    public void isEnabledInU() {
        assertThat(isNotProdStrategy.isEnabled(parameterForEnvironment("u"))).isTrue();
    }

    private HashMap<String, String> parameterForEnvironment(String environmentClass) {
        HashMap<String, String> map = new HashMap<>();
        map.put(ENVIRONMENT_NAME, environmentClass);
        return map;
    }
}