package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadRessursEndpointUtenOidcIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";


    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    @Test
    public void skalIkkeHaTilgangTilSeFakta() {
        SoknadTester soknadTester = soknadOpprettet();

        String url = "soknader/" + soknadTester.getBrukerBehandlingId() + "/fakta";
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

    }


}
