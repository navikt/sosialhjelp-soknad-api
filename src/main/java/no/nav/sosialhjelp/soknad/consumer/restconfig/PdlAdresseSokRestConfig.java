package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokConsumer;
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;

public class PdlAdresseSokRestConfig {

    private static final String PDLAPI_APIKEY = "PDLAPI_APIKEY";

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlAdresseSokConsumer pdlAdresseSokConsumer(STSConsumer stsConsumer) {
        return new PdlAdresseSokConsumer(pdlAdresseSokClient(), endpoint, stsConsumer);
    }

    private Client pdlAdresseSokClient() {
        final var apiKey = getenv(PDLAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
