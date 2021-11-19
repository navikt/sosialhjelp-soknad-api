package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.client.redis.RedisService;
import no.nav.sosialhjelp.soknad.client.sts.StsClient;
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumerImpl;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

public class PdlHentPersonRestConfig {

    private static final String PDLAPI_APIKEY = "PDLAPI_APIKEY";

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlHentPersonConsumer pdlHentPersonConsumer(StsClient stsClient, RedisService redisService) {
        return new PdlHentPersonConsumerImpl(pdlHentPersonClient(), endpoint, stsClient, redisService);
    }

    // Trenger kun en ping mot PDL
    @Bean
    public Pingable pdlRestPing(PdlHentPersonConsumer pdlHentPersonConsumer) {
        return () -> {
            var metadata = new PingMetadata(endpoint, "Pdl", false);
            try {
                pdlHentPersonConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client pdlHentPersonClient() {
        final var apiKey = getenv(PDLAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
