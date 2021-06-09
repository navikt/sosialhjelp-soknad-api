package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import static java.lang.System.getenv;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;

public abstract class PdlConfig {

    private static final String PDLAPI_APIKEY = "PDLAPI_APIKEY";

    protected Client pdlClient() {
        final var apiKey = getenv(PDLAPI_APIKEY);
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, apiKey));
    }
}
