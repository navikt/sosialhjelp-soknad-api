package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class FullOppsummeringRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgang_hentOppsummeringNew() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER)) //fake annen bruker, se FakeLoginFilter
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void nektetTilgang_fullSoknad() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknad";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER)) //fake annen bruker, se FakeLoginFilter
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void nektetTilgang_fullSoknadPdf() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget
                .queryParam("fnr", ANNEN_BRUKER)) //fake annen bruker, se FakeLoginFilter
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
