package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.inntektsogskatteopplysninger.InntektOgSkatteopplysningerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.inntektogskatteopplysninger.InntektOgSkatteopplysningerConsumerImpl;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class InntektsOgSkatteopplysningerRestConfig {


    public static final String WITHMOCK = "start.inntektogskatteopplysninger.withmock";

    @Value("${skatteetaten.inntektsmottaker}")
    public String endpoint;

    private final RestCallContext context = new RestCallContext.Builder()
            .withClient(RestUtils.createClient(RestUtils.RestConfig.builder().readTimeout(30000).build()))
            .withConcurrentRequests(2)
            .withMaximumQueueSize(6)
            .withExecutorTimeoutInMilliseconds(30000)
            .build();

    private final Function<InntektOgskatteopplysningerConsumer.Sokedata, RestCallContext> restCallContextSelector = (sokedata) -> context;

    @Bean
    public InntektOgskatteopplysningerConsumer inntektOgskatteopplysningerConsumer() {
        InntektOgskatteopplysningerConsumer prod = new InntektOgSkatteopplysningerConsumerImpl(restCallContextSelector, endpoint);
        InntektOgskatteopplysningerConsumer mock = new InntektOgSkatteopplysningerMock().inntektOgSkatteopplysningerRestService();
        return createSwitcher(prod, mock, WITHMOCK, InntektOgskatteopplysningerConsumer.class);
    }

    @Bean
    public Pingable inntektOgskatteopplysningerPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Skatteetaten-inntekt og skatteopplysninger ping", false);
                try {
                    InntektsOgSkatteopplysningerRestConfig.this.inntektOgskatteopplysningerConsumer().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
