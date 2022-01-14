package no.nav.sosialhjelp.soknad.web.integration.security;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.token.support.core.JwtTokenConstants;
import no.nav.sosialhjelp.soknad.web.integration.AbstractIT;
import no.nav.sosialhjelp.soknad.web.integration.SoknadTester;
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("migrer i-tester til springboottest")
class MineSakerMetadataRessursEndpointIT extends AbstractIT {

    public static final String BRUKER = "11111111111";
    public static final String ANNEN_BRUKER = "22222222222";

    @Test
    void pabegynte_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "minesaker/innsendte";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void pabegynte_skalGi401MedAnnenIssuer() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "minesaker/innsendte";
        SignedJWT signedJWTMedAnnenIssuer = JwtTokenGenerator.createSignedJWT(BRUKER);

        Response response = sendGetRequest(soknadTester, subUrl, signedJWTMedAnnenIssuer);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
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
