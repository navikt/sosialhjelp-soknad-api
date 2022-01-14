package no.nav.sosialhjelp.soknad.web.integration.security;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.token.support.core.JwtTokenConstants;
import no.nav.sosialhjelp.soknad.web.integration.AbstractIT;
import no.nav.sosialhjelp.soknad.web.integration.SoknadTester;
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("migrer i-tester til springboottest")
class SoknadActionsEndpointIT extends AbstractIT {

    private static final String DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN = "22222222222";

    @Test
    void sendSoknad_skalGiForbiddenMedAnnenBruker() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN);

        Response responseForAnnenBruker = sendPostRequest(soknadTester, subUrl, Entity.json(""), signedJWTforAnnenBruker.serialize(), null);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void sendSoknad_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadOpprettet();
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";

        Response response = sendPostRequest(soknadTester, subUrl, Entity.json(""), null, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    private Response sendGetRequest(SoknadTester soknadTester, String subUrl, String token){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(token != null) {
            builder.header(JwtTokenConstants.AUTHORIZATION_HEADER, "Bearer " + token);
        }

        return builder.buildGet()
                .invoke();
    }

    private Response sendPostRequest(SoknadTester soknadTester, String subUrl, Entity<String> entity, String token, String xhrHeader){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(token != null) {
            builder.header(JwtTokenConstants.AUTHORIZATION_HEADER, "Bearer " + token);
        }
        if(xhrHeader != null) {
            builder.header("X-XSRF-TOKEN", xhrHeader);
        }

        return builder.buildPost(entity)
                .invoke();
    }
}
