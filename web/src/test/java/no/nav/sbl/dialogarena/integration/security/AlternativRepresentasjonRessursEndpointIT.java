package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonRessursEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "***REMOVED***";
    private String skjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgangUtenToken_xmlRepresentasjon() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String url = "representasjon/xml/" + soknadTester.getBrukerBehandlingId();
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void nektetTilgangUtenToken_jsonRepresentasjon() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String url = "representasjon/json/" + soknadTester.getBrukerBehandlingId();
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
