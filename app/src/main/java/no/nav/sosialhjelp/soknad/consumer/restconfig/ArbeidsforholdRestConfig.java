package no.nav.sosialhjelp.soknad.consumer.restconfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdConsumerMock;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;

@Configuration
public class ArbeidsforholdRestConfig {

    public static final String AAREG_KEY = "start.aareg.withmock";
    private static final String SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD";

    @Value("${aareg_api_baseurl}")
    private String endpoint;

    @Inject
    private STSConsumer stsConsumer;

    private ObjectMapper arbeidsforholdMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public ArbeidsforholdConsumer arbeidsforholdConsumer() {
        ArbeidsforholdConsumer prod = new ArbeidsforholdConsumerImpl(arbeidsforholdClient(), endpoint, stsConsumer);
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
