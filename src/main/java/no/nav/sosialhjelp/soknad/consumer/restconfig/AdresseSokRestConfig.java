package no.nav.sosialhjelp.soknad.consumer.restconfig;


import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokConsumer;
import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils.RestConfig;
import no.nav.sosialhjelp.soknad.consumer.concurrency.RestCallContext;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;


@Configuration
public class AdresseSokRestConfig {

    @Value("${tps_adresse_url}")
    private String endpoint;

    private final RestCallContext medPostnummerExecutionContext = new RestCallContext.Builder()
            .withClient(RestUtils.createClient(RestConfig.builder().readTimeout(30000).build()))
            .withConcurrentRequests(3)
            .withMaximumQueueSize(9)
            .withExecutorTimeoutInMilliseconds(30000)
            .build();

    private final RestCallContext utenPostnummerExecutionContext = new RestCallContext.Builder()
            .withClient(RestUtils.createClient(RestConfig.builder().readTimeout(30000).build()))
            .withConcurrentRequests(2)
            .withMaximumQueueSize(6)
            .withExecutorTimeoutInMilliseconds(30000)
            .build();

    private final Function<Sokedata, RestCallContext> restCallContextSelector = (sokedata) -> (sokedata != null && sokedata.postnummer != null) ? medPostnummerExecutionContext : utenPostnummerExecutionContext;

    @Bean
    public AdresseSokConsumer adresseSokConsumer() {
        return new AdresseSokConsumerImpl(restCallContextSelector, endpoint);
    }

    @Bean
    public Pingable adressesokPing() {
        return () -> {
            PingMetadata metadata = new PingMetadata(endpoint, "TPSWS-adressesok", false);
            try {
                adresseSokConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
