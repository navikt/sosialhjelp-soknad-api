package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokConsumerImpl;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;


@Configuration
public class AdresseSokRestConfig {

    public static final String ADRESSE_KEY = "start.adressesok.withmock";

    @Value("${tps.adresse.url}")
    private String endpoint;

    @Bean
    public AdresseSokConsumer adresseSokConsumer() {
        AdresseSokConsumerImpl prod = new AdresseSokConsumerImpl(RestUtils.createClient(), endpoint);
        AdresseSokConsumer mock = new AdresseSokConsumerMock().adresseRestService();
        return createSwitcher(prod, mock, ADRESSE_KEY, AdresseSokConsumer.class);
    }
}
