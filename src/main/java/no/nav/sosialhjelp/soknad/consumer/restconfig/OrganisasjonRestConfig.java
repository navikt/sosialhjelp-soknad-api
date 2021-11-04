//package no.nav.sosialhjelp.soknad.consumer.restconfig;
//
//import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
//import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonConsumer;
//import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonConsumerImpl;
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
//public class OrganisasjonRestConfig {
//
//    private static final String EREGAPI_APIKEY = "EREGAPI_APIKEY";
//
//    @Value("${ereg_api_baseurl}")
//    private String endpoint;
//
//    @Bean
//    public OrganisasjonConsumer organisasjonConsumer() {
//        return new OrganisasjonConsumerImpl(organisasjonClient(), endpoint);
//    }
//
//    @Bean
//    public Pingable organisasjonRestPing() {
//        return () -> {
//            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "Organisasjon", false);
//            try {
//                organisasjonConsumer().ping();
//                return lyktes(metadata);
//            } catch (Exception e) {
//                return feilet(metadata, e);
//            }
//        };
//    }
//
//    private Client organisasjonClient() {
//        final String apiKey = getenv(EREGAPI_APIKEY);
//        return RestUtils.createClient()
//                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
//    }
//}
