package no.nav.sbl.dialogarena.integration.security;


import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Entity;
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
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;

public class FaktaRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClass(true);
    }

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void nektetTilgangUtenToken_opprettFaktum() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response response = sendPostRequest(soknadTester, signedJWT, Entity.json(faktum()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void opprettFaktum_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);

        Response response = sendPostRequest(soknadTester, null, Entity.json(faktum()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    private static final Faktum faktum() {
        Faktum faktum = new Faktum();
        faktum.setKey("annetfaktum");
        faktum.setValue("Test");
        return faktum;
    }

    private Response sendGetRequest(SoknadTester soknadTester, SignedJWT signedJWT){
        Invocation.Builder builder = soknadTester.sendsoknadResource("fakta/1", webTarget -> webTarget);

        if(signedJWT != null) {
            builder.header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize());
        }

        return builder.buildGet()
                .invoke();
    }

    private Response sendPostRequest(SoknadTester soknadTester, SignedJWT signedJWT, Entity entity ){
        Invocation.Builder builder = soknadTester.sendsoknadResource("fakta", webTarget -> webTarget
                .queryParam("behandlingsId", soknadTester.getBrukerBehandlingId()));

        if(signedJWT != null) {
            builder.header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize());
        }

        return builder.buildPost(entity)
                .invoke();
    }

}
