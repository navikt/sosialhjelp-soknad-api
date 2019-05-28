package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonRessursEndpointUtenOidcIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "10108000398";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    @Test
    public void accessDeniedMedAnnenBruker_jsonRepresentasjon() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        Response responseUtenFnr = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseUtenFnr.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
