package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonConsumerImpl;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class OrganisasjonRestConfig {

    public static final String ORGANISASJON_KEY = "start.organisasjon.withmock";

    @Value("${ereg_api_baseurl}")
    private String endpoint;

    @Bean
    public OrganisasjonConsumer organisasjonConsumer() {
        OrganisasjonConsumer prod = new OrganisasjonConsumerImpl(RestUtils.createClient(), endpoint);
        OrganisasjonConsumer mock = new OrganisasjonConsumerMock().organisasjonConsumerMock();
        return createSwitcher(prod, mock, ORGANISASJON_KEY, OrganisasjonConsumer.class);
    }

    @Bean
    public Pingable organisasjonRestPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Organisasjon", false);
            try {
                organisasjonConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
