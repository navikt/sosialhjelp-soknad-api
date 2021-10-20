package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumer;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
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
public class DkifRestConfig {

    private static final String DKIFAPI_APIKEY = "DKIFAPI_APIKEY";

    @Value("${dkif_api_baseurl}")
    private String endpoint;

    @Bean
    public DkifConsumer dkifConsumer(RedisService redisService) {
        return new DkifConsumerImpl(dkifClient(), endpoint, redisService);
    }

    @Bean
    public Pingable dkifRestPing(DkifConsumer dkifConsumer) {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Dkif", false);
            try {
                dkifConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client dkifClient() {
        final String apiKey = getenv(DKIFAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
