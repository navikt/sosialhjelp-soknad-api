package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadActionsEndpointUtenOidcIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "01010112345";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void leggVedVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
            webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendSoknad() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_fortsettsenere() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        Response responseMedBrukersEgenXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken("BRUKER_2_SIN_BEHANDLINGSID"))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedBrukersEgenXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

        Response responseMedStjeltXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedStjeltXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_bekreftinnsending() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
