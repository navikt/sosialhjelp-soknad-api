package no.nav.sosialhjelp.soknad.web.integration.security;


import com.nimbusds.jwt.SignedJWT;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons;
import no.nav.security.token.support.core.JwtTokenConstants;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sosialhjelp.soknad.web.integration.AbstractIT;
import no.nav.sosialhjelp.soknad.web.integration.SoknadTester;
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SaksoversiktMetadataOidcRessursEndpointIT extends AbstractIT {
    public static final String BRUKER = "11111111111";
    public static final String ANNEN_BRUKER = "22222222222";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

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
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(signedJWT != null) {
            builder.header(JwtTokenConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize());
        }

        return builder.buildGet()
                .invoke();
    }
}
