package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdConsumerImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdConsumerMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class ArbeidsforholdRestConfig {

    public static final String AAREG_KEY = "start.aareg.withmock";
    private static final String SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD";

    @Value("${aareg_api_baseurl}")
    private String endpoint;

    private ObjectMapper arbeidsforholdMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public ArbeidsforholdConsumer arbeidsforholdConsumer() {
        ArbeidsforholdConsumer prod = new ArbeidsforholdConsumerImpl(arbeidsforholdClient(), endpoint);
        ArbeidsforholdConsumer mock = new ArbeidsforholdConsumerMock().arbeidsforholdConsumerMock();
        return createSwitcher(prod, mock, AAREG_KEY, ArbeidsforholdConsumer.class);
    }

    @Bean
    public Pingable arbeidsforholdRestPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Aareg", false);
            try {
                arbeidsforholdConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client arbeidsforholdClient() {
        final String apiKey = getenv(SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey))
                .register(arbeidsforholdMapper());
    }
}