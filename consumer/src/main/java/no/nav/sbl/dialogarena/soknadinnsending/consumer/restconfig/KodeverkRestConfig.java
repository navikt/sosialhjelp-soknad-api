package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkConsumerImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkConsumerMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class KodeverkRestConfig {

    public static final String KODEVERK_KEY = "start.kodeverk.withmock";
    private static final String SOSIALHJELP_SOKNAD_API_KODEVERKAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_KODEVERKAPI_APIKEY_PASSWORD";

    @Value("${kodeverk_api_url}")
    private String endpoint;

    @Bean
    public KodeverkConsumer kodeverkConsumer() {
        KodeverkConsumer prod = new KodeverkConsumerImpl(kodeverkClient(), endpoint);
        KodeverkConsumer mock = new KodeverkConsumerMock().kodeverkConsumerMock();
        return createSwitcher(prod, mock, KODEVERK_KEY, KodeverkConsumer.class);
    }

    @Bean
    public Pingable kodeverkRestPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Kodeverk", false);
            try {
                kodeverkConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client kodeverkClient() {
        final String apiKey = getenv(SOSIALHJELP_SOKNAD_API_KODEVERKAPI_APIKEY_PASSWORD);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
