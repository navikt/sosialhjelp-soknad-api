package no.nav.sbl.dialogarena.integration.security;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;

public class SoknadActionsEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "***REMOVED***";
    private String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void leggVedVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendSoknad() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_fortsettsenere() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String token = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER).serialize();
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        Response responseMedBrukersEgenXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken("BRUKER_2_SIN_BEHANDLINGSID", new DateTime().toString("yyyyMMdd"), token))
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token)
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedBrukersEgenXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

        Response responseMedStjeltXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token)
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedStjeltXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void sendEpost_bekreftinnsending() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void leggVedVedlegg_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void sendSoknad_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void fortsettsenere_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void bekreftinnsending_skalGi401UtenToken() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget)
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
