//package no.nav.sosialhjelp.soknad.consumer.restconfig;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkConsumer;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkConsumerImpl;
//import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
//import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientRequestFilter;
//
//import static java.lang.System.getenv;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;
//
//@Configuration
//public class KodeverkRestConfig {
//
//    private static final String KODEVERKAPI_APIKEY = "KODEVERKAPI_APIKEY";
//
//    @Value("${kodeverk_api_url}")
//    private String endpoint;
//
//    @Bean
//    public KodeverkConsumer kodeverkConsumer(RedisService redisService) {
//        return new KodeverkConsumerImpl(kodeverkClient(), endpoint, redisService);
//    }
//
//    @Bean
//    public Pingable kodeverkRestPing(KodeverkConsumer kodeverkConsumer) {
//        return () -> {
//            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Kodeverk", false);
//            try {
//                kodeverkConsumer.ping();
//                return lyktes(metadata);
//            } catch (Exception e) {
//                return feilet(metadata, e);
//            }
//        };
//    }
//
//    public static ObjectMapper kodeverkMapper() {
//        return new ObjectMapper()
//                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
//                .registerModule(new JavaTimeModule());
//    }
//
//    private Client kodeverkClient() {
//        final String apiKey = getenv(KODEVERKAPI_APIKEY);
//        return RestUtils.createClient()
//                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey))
//                .register(kodeverkMapper());
//    }
//}
