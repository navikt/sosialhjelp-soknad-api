package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.web.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.consumer.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.lyktes;

public class OppslagRestConfig {
    private static final String OPPSLAGAPI_APIKEY = "OPPSLAGAPI_APIKEY";
    public static final String OPPSLAG_KEY = "start.oppslag.withmock";

    @Value("${oppslag_api_baseurl}")
    private String endpoint;

    @Bean
    public OppslagConsumer kontonummerConsumer() {
        var prod = new OppslagConsumerImpl(oppslagClient(), endpoint);
        var mock = new OppslagConsumerMock().oppslagMock();
        return createSwitcher(prod, mock, OPPSLAG_KEY, OppslagConsumer.class);
    }

    @Bean
    public Pingable kontonummerPing(OppslagConsumer oppslagConsumer) {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Oppslag", false);
            try {
                oppslagConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client oppslagClient() {
        final var apiKey = getenv(OPPSLAGAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
