package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "22222222222";

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgang_opprettEttersendelse() {
        SoknadTester soknadTester = soknadOpprettet();
        String url = "soknader/opprettSoknad";

        Response response = soknadTester.sendsoknadResource(url, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER)
                .queryParam("ettersendTil", soknadTester.getBrukerBehandlingId() )) //fake annen bruker, se FakeLoginFilter
                .buildPost(null)
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
