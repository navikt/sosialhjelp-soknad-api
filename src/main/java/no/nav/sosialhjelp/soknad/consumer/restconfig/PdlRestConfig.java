package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import no.nav.sosialhjelp.soknad.web.types.Pingable;
import no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.lyktes;

public class PdlRestConfig {

    private static final String PDLAPI_APIKEY = "PDLAPI_APIKEY";

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlConsumer pdlConsumer(STSConsumer stsConsumer) {
        return new PdlConsumerImpl(pdlClient(), endpoint, stsConsumer);
    }

    @Bean
    public Pingable pdlRestPing(PdlConsumer pdlConsumer) {
        return () -> {
            PingMetadata metadata = new PingMetadata(endpoint, "Pdl", false);
            try {
                pdlConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client pdlClient() {
        final String apiKey = getenv(PDLAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
