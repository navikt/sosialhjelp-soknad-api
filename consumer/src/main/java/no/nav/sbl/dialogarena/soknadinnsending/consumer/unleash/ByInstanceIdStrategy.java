package no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash;

import no.finn.unleash.strategy.Strategy;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static java.lang.System.getProperty;

public class ByInstanceIdStrategy implements Strategy {

    private static final String INSTANCE_PROPERTY = "unleash_instance_id";

    @Override
    public String getName() {
        return "byInstanceId";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return Optional.ofNullable(parameters)
                .map(par -> par.get("instance.id"))
                .filter(s -> !s.isEmpty())
                .map(instance -> instance.split(","))
                .map(Arrays::stream)
                .map(instance -> instance.anyMatch(this::isCurrentInstance))
                .orElse(false);
    }

    private boolean isCurrentInstance(String instance) {
        return getProperty(INSTANCE_PROPERTY, "local").equals(instance);
    }
}
