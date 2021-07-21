package no.nav.sosialhjelp.soknad.consumer.unleash;

import no.finn.unleash.strategy.Strategy;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


public class ByInstanceIdStrategy implements Strategy {

    private final String instanceId;

    public ByInstanceIdStrategy(String instanceId) {
        this.instanceId = instanceId;
    }

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
        return instanceId.equals(instance);
    }
}
