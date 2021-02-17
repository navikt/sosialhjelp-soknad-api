package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumer;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumerMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;

@Configuration
public class DkifRestConfig {

    private static final String SOSIALHJELP_SOKNAD_API_DKIFAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_DKIFAPI_APIKEY_PASSWORD";
    public static final String DKIF_KEY = "start.dkif.withmock";

    @Value("${dkif_api_baseurl}")
    private String endpoint;

    @Bean
    public DkifConsumer dkifConsumer() {
        DkifConsumer prod = new DkifConsumerImpl(dkifClient(), endpoint);
        DkifConsumer mock = new DkifConsumerMock().dkifConsumerMock();
        return createSwitcher(prod, mock, DKIF_KEY, DkifConsumer.class);
    }

    @Bean
    public Pingable dkifRestPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Dkif", false);
            try {
                dkifConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client dkifClient() {
        final String apiKey = getenv(SOSIALHJELP_SOKNAD_API_DKIFAPI_APIKEY_PASSWORD);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
