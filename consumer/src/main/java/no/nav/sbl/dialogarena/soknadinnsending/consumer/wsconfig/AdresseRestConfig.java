package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseConsumer;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseConsumerImpl;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;


@Configuration
public class AdresseRestConfig {

    public static final String ADRESSE_KEY = "start.adresse.withmock";

    @Value("${tps.adresse.url}")
    private String endpoint;

    @Bean
    public AdresseConsumer adresseConsumer() {
        AdresseConsumerImpl prod = new AdresseConsumerImpl(RestUtils.createClient(), endpoint);
        AdresseConsumer mock = new AdresseConsumerMock().adresseRestService();
        return createSwitcher(prod, mock, ADRESSE_KEY, AdresseConsumer.class);
    }
}
