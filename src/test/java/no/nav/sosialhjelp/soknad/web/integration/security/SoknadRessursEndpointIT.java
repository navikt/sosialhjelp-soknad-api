package no.nav.sosialhjelp.soknad.web.integration.security;


import no.nav.sosialhjelp.soknad.web.integration.AbstractIT;
import no.nav.sosialhjelp.soknad.web.integration.SoknadTester;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

class SoknadRessursEndpointIT extends AbstractIT {
    static final String ANNEN_BRUKER = "22222222222";

    @Test
    void nektetTilgang_opprettEttersendelse() {
        SoknadTester soknadTester = soknadOpprettet();
        String url = "soknader/opprettSoknad";

        Response response = soknadTester.sendsoknadResource(url, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER)
                .queryParam("ettersendTil", soknadTester.getBrukerBehandlingId() ))
                .buildPost(null)
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
