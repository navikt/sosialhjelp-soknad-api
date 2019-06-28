package no.nav.sbl.dialogarena.integration.security;

import com.nimbusds.jwt.SignedJWT;
import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonRessursEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "10108000398";


    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClass(true);
    }

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    /* JsonRepresentasjon */

    @Test
    public void jsonRepresentasjon_skalGiServerErrorMedAnnenBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response responseForAnnenBruker = sendGetRequest(soknadTester, subUrl, signedJWTforAnnenBruker);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void jsonRepresentasjon_skalIkkeGiForbiddenMedRettBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(soknadTester.getUser());

        Response response = sendGetRequest(soknadTester, subUrl, signedJWT);

        assertThat(response.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void jsonRepresentasjon_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();

        Response response = sendGetRequest(soknadTester, subUrl, null);

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
