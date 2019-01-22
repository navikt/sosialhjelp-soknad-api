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

public class FullOppsummeringRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void hentOppsummeringNew() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(soknadTester.getUser());
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";
        Response responseForAnnenBruker = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWTforAnnenBruker.serialize())
                .buildGet()
                .invoke();
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildGet()
                .invoke();

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void fullSoknadPdf() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWTforAnnenBruker.serialize())
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void fullSoknadPdf_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/fullsoknadpdf";
        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void hentOppsummeringNew_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String suburl = "fulloppsummering/" + soknadTester.getBrukerBehandlingId() + "/nyoppsummering";

        Response response = soknadTester.sendsoknadResource(suburl, webTarget -> webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
