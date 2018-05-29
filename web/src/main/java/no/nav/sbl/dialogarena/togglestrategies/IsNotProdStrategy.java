package no.nav.sbl.dialogarena.togglestrategies;

import no.finn.unleash.strategy.Strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IsNotProdStrategy implements Strategy {

    public static final String ENVIRONMENT_NAME = "environment.name";
    public static final List<String> prodEnvs = Arrays.asList("p", "q0");

    @Override
    public String getName() {
        return "isNotProd";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return !prodEnvs.contains(parameters.get(ENVIRONMENT_NAME));
    }
}
