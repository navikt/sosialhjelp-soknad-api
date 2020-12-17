package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.repository.FeatureToggleResponse.Status;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleFetcher;
import no.finn.unleash.util.UnleashConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash.ByInstanceIdStrategy;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static no.finn.unleash.repository.FeatureToggleResponse.Status.CHANGED;
import static no.finn.unleash.repository.FeatureToggleResponse.Status.NOT_CHANGED;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

public class UnleashRestConfig {
    public static final String MOCK_KEY = "start.unleash.withmock";
    public static final String APPLICATION_NAME = "sosialhjelp-soknad-api";

    @Value("${unleash_api_url}")
    private String endpoint;

    @Value("${unleash_instance_id}")
    private String instanceId;

    @Bean
    public Unleash unleashConsumer() {
        Unleash prod = new DefaultUnleash(config(), new ByInstanceIdStrategy());
        Unleash mock = new FakeUnleash();
        return createSwitcher(prod, mock, MOCK_KEY, Unleash.class);
    }

    @Bean
    public ToggleFetcher unleashToggleFetcher() {
        return new HttpToggleFetcher(config());
    }

    @Bean
    public Pingable unleashRestPing(ToggleFetcher unleashToggleFetcher) {
        return () -> {
            PingMetadata metadata = new PingMetadata("unleash_ping", endpoint, "Unleash", false);
            try {
                Status status = unleashToggleFetcher.fetchToggles().getStatus();
                if (status == CHANGED || status == NOT_CHANGED) {
                    return lyktes(metadata);
                } else {
                    return feilet(metadata, "Ping mot Unleash på " + endpoint + ". Ga status " + status);
                }
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private UnleashConfig config() {
        return UnleashConfig.builder()
                .appName(APPLICATION_NAME)
                .instanceId(instanceId)
                .unleashAPI(endpoint)
                .build();
    }
}
