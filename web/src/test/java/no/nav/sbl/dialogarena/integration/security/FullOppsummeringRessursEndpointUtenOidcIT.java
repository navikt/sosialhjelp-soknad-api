package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class FullOppsummeringRessursEndpointUtenOidcIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void hentOppsummeringNew() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();
        Response responseUtenFnr = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(responseUtenFnr.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void fullSoknadPdf() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
