//package no.nav.sosialhjelp.soknad.consumer.restconfig;
//
//import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
//import no.nav.sosialhjelp.soknad.consumer.norg.NorgConsumer;
//import no.nav.sosialhjelp.soknad.consumer.norg.NorgConsumerImpl;
//import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
//import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
//import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
//import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;
//
//
//@Configuration
//public class NorgRestConfig {
//
//    @Value("${norg_rest_url}")
//    private String endpoint;
//
//    @Bean
//    public NorgConsumer norgConsumer(RedisService redisService) {
//        return new NorgConsumerImpl(RestUtils.createClient(), endpoint, redisService);
//    }
//
//    @Bean
//    public Pingable norgRestPing(NorgConsumer norgConsumer) {
//        return () -> {
//            PingMetadata metadata = new PingMetadata(endpoint, "Norg2", false);
//            try {
//                norgConsumer.ping();
//                return lyktes(metadata);
//            } catch (Exception e) {
//                return feilet(metadata, e);
//            }
//        };
//    }
//}
