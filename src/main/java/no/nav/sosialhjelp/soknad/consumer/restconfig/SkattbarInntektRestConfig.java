package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumer;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumerImpl;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

@Configuration
public class SkattbarInntektRestConfig {

    private static final String SKATT_INNTEKTSMOTTAKER_APIKEY = "SKATT_INNTEKTSMOTTAKER_APIKEY";

    @Value("${skatteetaten_inntektsmottaker_url}")
    private String endpoint;

    @Bean
    public SkattbarInntektConsumer skattbarInntektConsumer() {
        return new SkattbarInntektConsumerImpl(skattbarInntektClient(), endpoint);
    }

    @Bean
    public Pingable skattbarInntektRestPing(SkattbarInntektConsumer skattbarInntektConsumer) {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Skatteetaten", false);
            try {
                skattbarInntektConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client skattbarInntektClient() {
        final String apiKey = getenv(SKATT_INNTEKTSMOTTAKER_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
