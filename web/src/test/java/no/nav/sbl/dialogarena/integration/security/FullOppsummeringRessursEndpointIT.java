package no.nav.sbl.dialogarena.integration.security;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;

public class FullOppsummeringRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClass(true);
    }

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void hentOppsummeringNew_skalGiServerErrorMedAnnenBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response responseForAnnenBruker = sendGetRequest(soknadTester, suburl, signedJWTforAnnenBruker);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
    @Test
    public void hentOppsummeringNew_skalIkkeGiForbiddenMedRettBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(soknadTester.getUser());

        Response response = sendGetRequest(soknadTester, suburl, signedJWT);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void fullSoknadPdf() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response responseForAnnenBruker = sendGetRequest(soknadTester, suburl, signedJWTforAnnenBruker);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void fullSoknadPdf_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";

        Response response = sendGetRequest(soknadTester, suburl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void hentOppsummeringNew_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";

        Response response = sendGetRequest(soknadTester, suburl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    private Response sendGetRequest(SoknadTester soknadTester, String subUrl, SignedJWT signedJWT){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(signedJWT != null) {
            builder.header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize());
        }

        return builder.buildGet()
                .invoke();
    }
}
