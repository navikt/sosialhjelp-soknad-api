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

public class SoknadRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;


    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    @Test
    public void skalIkkeHaTilgangTilSeFakta() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        String url = "soknader/" + soknadTester.getBrukerBehandlingId() + "/fakta";
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

    }

    @Test
    public void fakta_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);

        String url = "soknader/" + soknadTester.getBrukerBehandlingId() + "/fakta";
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());

    }


}
