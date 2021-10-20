package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

public class OppslagRestConfig {
    private static final String OPPSLAGAPI_APIKEY = "OPPSLAGAPI_APIKEY";

    @Value("${oppslag_api_baseurl}")
    private String endpoint;

    @Bean
    public OppslagConsumer oppslagConsumer(RedisService redisService) {
        return new OppslagConsumerImpl(oppslagClient(), endpoint, redisService);
    }

    @Bean
    public Pingable oppslagPing(OppslagConsumer oppslagConsumer) {
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
