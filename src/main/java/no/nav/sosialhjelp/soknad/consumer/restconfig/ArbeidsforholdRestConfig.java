//package no.nav.sosialhjelp.soknad.consumer.restconfig;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdConsumer;
//import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.ArbeidsforholdConsumerImpl;
//import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
//import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
//import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.inject.Inject;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientRequestFilter;
//
//import static java.lang.System.getenv;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;
//
//@Configuration
//public class ArbeidsforholdRestConfig {
//
//    private static final String AAREGAPI_APIKEY = "AAREGAPI_APIKEY";
//
//    @Value("${aareg_api_baseurl}")
//    private String endpoint;
//
//    @Inject
//    private STSConsumer stsConsumer;
//
//    private ObjectMapper arbeidsforholdMapper() {
//        return new ObjectMapper()
//                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//                .registerModule(new JavaTimeModule());
//    }
//
//    @Bean
//    public ArbeidsforholdConsumer arbeidsforholdConsumer() {
//        return new ArbeidsforholdConsumerImpl(arbeidsforholdClient(), endpoint, stsConsumer);
//    }
//
//    @Bean
//    public Pingable arbeidsforholdRestPing() {
//        return () -> {
//            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Aareg", false);
//            try {
//                arbeidsforholdConsumer().ping();
//                return lyktes(metadata);
//            } catch (Exception e) {
//                return feilet(metadata, e);
//            }
//        };
//    }
//
//    private Client arbeidsforholdClient() {
//        final String apiKey = getenv(AAREGAPI_APIKEY);
//        return RestUtils.createClient()
//                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey))
//                .register(arbeidsforholdMapper());
//    }
//}
