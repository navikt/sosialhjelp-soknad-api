package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokConsumerImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.rest.RestUtils;
import no.nav.sbl.rest.RestUtils.RestConfig;


@Configuration
public class AdresseSokRestConfig {

    public static final String ADRESSE_KEY = "start.adressesok.withmock";

    @Value("${tps.adresse.url}")
    private String endpoint;
    
    /*
     * maximumFrontendTimeoutInMilliseconds = (timeoutInMilliseconds * maximumQueueSize) / concurrentRequests
     */
    
    private final RestCallContext medPostnummerExecutionContext = new RestCallContext.Builder()
            .withClient(RestUtils.createClient(RestConfig.builder().readTimeout(5000).build()))
            .withConcurrentRequests(2)
            .withMaximumQueueSize(8)
            .withTimeoutInMilliseconds(20000)
            .build();
    
    private final RestCallContext utenPostnummerExecutionContext = new RestCallContext.Builder()
            .withClient(RestUtils.createClient(RestConfig.builder().readTimeout(5000).build()))
            .withConcurrentRequests(1)
            .withMaximumQueueSize(4)
            .withTimeoutInMilliseconds(20000)
            .build();
    
    private final Function<Sokedata, RestCallContext> restCallContextSelector = (sokedata) -> {
        return (sokedata.postnummer != null) ? medPostnummerExecutionContext : utenPostnummerExecutionContext;
    };

    @Bean
    public AdresseSokConsumer adresseSokConsumer() {
        //AdresseSokConsumerImpl prod = new AdresseSokConsumerImpl(RestUtils.createClient(RestConfig.builder().readTimeout(5000).build()), endpoint);
        AdresseSokConsumerImpl prod = new AdresseSokConsumerImpl(restCallContextSelector, endpoint);
        AdresseSokConsumer mock = new AdresseSokConsumerMock().adresseRestService();
        return createSwitcher(prod, mock, ADRESSE_KEY, AdresseSokConsumer.class);
    }
    
    
}
