package no.nav.sbl.dialogarena.integration.security;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;

public class SoknadActionsEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "01010112345";
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
    public void leggVedVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response response = sendGetRequest(soknadTester, subUrl, signedJWT.serialize());

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendSoknad_skalGiForbiddenMedAnnenBruker() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        SignedJWT signedJWTforAnnenBruker = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response responseForAnnenBruker = sendPostRequest(soknadTester, subUrl, Entity.json(""), signedJWTforAnnenBruker.serialize(), null);

        assertThat(responseForAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_fortsettsenere_skalGiForbiddenMedAnnenBruker() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        String token = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER).serialize();

        Response responseMedBrukersEgenXSRFToken = sendPostRequest(soknadTester, subUrl, Entity.json(""), token, XsrfGenerator.generateXsrfToken("BRUKER_2_SIN_BEHANDLINGSID", new DateTime().toString("yyyyMMdd"), token));

        assertThat(responseMedBrukersEgenXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_fortsettsenere_skalGiForbiddenTilAnnenBrukerMedStjeltXSRF() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        String token = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER).serialize();

        Response responseMedStjeltXSRFToken = sendPostRequest(soknadTester, subUrl, Entity.json(""), token, soknadTester.getXhrHeader());

        assertThat(responseMedStjeltXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_bekreftinnsending() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);

        Response response = sendPostRequest(soknadTester, subUrl, Entity.json(""), signedJWT.serialize(), null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void leggVedVedlegg_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";

        Response response = sendGetRequest(soknadTester, subUrl, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void sendSoknad_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";

        Response response = sendPostRequest(soknadTester, subUrl, Entity.json(""), null, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void fortsettsenere_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";

        Response response = sendPostRequest(soknadTester, subUrl,  Entity.json(""), null, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void bekreftinnsending_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";

        Response response = sendPostRequest(soknadTester, subUrl, Entity.json(""), null, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    private Response sendGetRequest(SoknadTester soknadTester, String subUrl, String token){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(token != null) {
            builder.header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token);
        }

        return builder.buildGet()
                .invoke();
    }


    private Response sendPostRequest(SoknadTester soknadTester, String subUrl, Entity entity, String token, String xhrHeader){
        Invocation.Builder builder = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget);

        if(token != null) {
            builder.header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token);
        }
        if(xhrHeader != null) {
            builder.header("X-XSRF-TOKEN", xhrHeader);
        }

        return builder.buildPost(entity)
                .invoke();
    }
}
