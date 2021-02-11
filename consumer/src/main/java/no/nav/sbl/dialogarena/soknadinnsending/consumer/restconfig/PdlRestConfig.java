package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlConsumerImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.STSConsumer;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

public class PdlRestConfig {

    public static final String PDL_KEY = "start.pdl.withmock";
    private static final String PDLAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_PDLAPI_APIKEY_PASSWORD";

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlConsumer pdlConsumer(STSConsumer stsConsumer) {
        PdlConsumer prod = new PdlConsumerImpl(pdlClient(), endpoint, stsConsumer);
        PdlConsumer mock = new PdlConsumerMock().pdlConsumerMock();
        return createSwitcher(prod, mock, PDL_KEY, PdlConsumer.class);
    }

    @Bean
    public Pingable pdlRestPing(PdlConsumer pdlConsumer) {
        return () -> {
            PingMetadata metadata = new PingMetadata(endpoint, "Pdl", true);
            try {
                pdlConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client pdlClient() {
        final String apiKey = getenv(PDLAPI_APIKEY_PASSWORD);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
