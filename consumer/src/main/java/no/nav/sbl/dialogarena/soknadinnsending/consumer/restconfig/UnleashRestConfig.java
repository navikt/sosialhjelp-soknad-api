package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash.UnleashConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash.UnleashConsumerImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash.UnleashConsumerMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import javax.ws.rs.client.Client;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

public class UnleashRestConfig {
    public static final String MOCK_KEY = "start.unleash.withmock";

    @Value("${unleash_api_url}")
    private String endpoint;

    @Bean
    public UnleashConsumer unleashConsumer() {
        UnleashConsumer prod = new UnleashConsumerImpl(unleashClient(), endpoint);
        UnleashConsumer mock = new UnleashConsumerMock().unleashConsumerMock();
        return createSwitcher(prod, mock, MOCK_KEY, UnleashConsumer.class);
    }

    @Bean
    public Pingable unleashRestPing(UnleashConsumer unleashConsumer) {
        return () -> {
            PingMetadata metadata = new PingMetadata("unleash_ping", endpoint, "Unleash", false);
            try {
                unleashConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client unleashClient() {
        return RestUtils.createClient();
    }
}
