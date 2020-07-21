package no.nav.sbl.dialogarena.integration.security;


import com.nimbusds.jwt.SignedJWT;
import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons;
import no.nav.security.token.support.core.JwtTokenConstants;
import no.nav.security.token.support.test.JwtTokenGenerator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SaksoversiktMetadataOidcRessursEndpointIT extends AbstractSecurityIT {
    public static final String BRUKER = "11111111111";
    public static final String ANNEN_BRUKER = "12345679811";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Mock
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @Before
    public void setup() throws Exception {
        //when(subjectHandlerWrapper.getIdent()).thenReturn(BRUKER);
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void innsendte_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "metadata/oidc/innsendte";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void ettersendelse_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "metadata/oidc/ettersendelse";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void pabegynte_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "metadata/oidc/pabegynte";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Ignore("Fiks FakeLoginFilter slik at den håndterer tokens riktig")
    @Test
    public void skalIkkeSePabegynteForAnnenBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "metadata/oidc/pabegynte";
        SignedJWT signedJWTForAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response responseForAnnenBruker = sendGetRequest(soknadTester, subUrl, signedJWTForAnnenBruker);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        PabegynteSoknaderRespons body = responseForAnnenBruker.readEntity(PabegynteSoknaderRespons.class);
        assertThat(body.getPabegynteSoknader()).isEmpty();
    }

    @Test
    public void ping_skalGi200UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "metadata/oidc/ping";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private Response sendGetRequest(SoknadTester soknadTester, String subUrl, SignedJWT signedJWT){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget
        );

        if(signedJWT != null) {
            builder.header(JwtTokenConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize());
        }

        return builder.buildGet()
                .invoke();
    }
}
