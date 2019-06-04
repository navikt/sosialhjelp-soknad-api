package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadActionsEndpointUtenOidcIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "01010112345";

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void sendSoknad() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
