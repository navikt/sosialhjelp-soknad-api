package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumer;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumerMock;
import no.nav.sosialhjelp.soknad.web.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.consumer.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.lyktes;

@Configuration
public class SkattbarInntektRestConfig {

    private static final String SOKNADSOSIALHJELP_SERVER_SKATT_INNTEKTSMOTTAKER_APIKEY_PASSWORD = "SOKNADSOSIALHJELP_SERVER_SKATT_INNTEKTSMOTTAKER_APIKEY_PASSWORD";
    public static final String SKATT_KEY = "start.inntektogskatteopplysninger.withmock";

    @Value("${skatteetaten.inntektsmottaker.url}")
    private String endpoint;

    @Bean
    public SkattbarInntektConsumer skattbarInntektConsumer() {
        SkattbarInntektConsumer prod = new SkattbarInntektConsumerImpl(skattbarInntektClient(), endpoint);
        SkattbarInntektConsumer mock = new SkattbarInntektConsumerMock().skattbarInntektConsumerMock();
        return createSwitcher(prod, mock, SKATT_KEY, SkattbarInntektConsumer.class);
    }

    @Bean
    public Pingable skattbarInntektRestPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Skatteetaten", false);
            try {
                skattbarInntektConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client skattbarInntektClient() {
        final String apiKey = getenv(SOKNADSOSIALHJELP_SERVER_SKATT_INNTEKTSMOTTAKER_APIKEY_PASSWORD);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
