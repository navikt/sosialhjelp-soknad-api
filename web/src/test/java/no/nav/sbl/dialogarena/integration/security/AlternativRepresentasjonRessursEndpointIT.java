package no.nav.sbl.dialogarena.integration.security;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.junit.Before;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;

public class AlternativRepresentasjonRessursEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "***REMOVED***";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void accessDeniedMedAnnenBruker_xmlRepresentasjon() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(soknadTester.getUser());
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String subUrl = "representasjon/xml/" + soknadTester.getBrukerBehandlingId();
        Response responseForAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWTforAnnenBruker.serialize())
                .buildGet()
                .invoke();

        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildGet()
                .invoke();

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void accessDeniedMedAnnenBruker_jsonRepresentasjon() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(soknadTester.getUser());
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();
        Response responseForAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWTforAnnenBruker.serialize())
                .buildGet()
                .invoke();

        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildGet()
                .invoke();

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void jsonRepresentasjon_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "representasjon/json/" + soknadTester.getBrukerBehandlingId();

        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
