package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.security.token.support.core.JwtTokenConstants;
import no.nav.security.token.support.test.JwtTokenGenerator;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Function;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.apache.http.HttpStatus.SC_OK;

public class SoknadTester extends JerseyTest {
    private String user;
    private String token;

    private String brukerBehandlingId;

    private SoknadTester() {
        super();
        this.user = "11111111111";
        this.token = JwtTokenGenerator.createSignedJWT(this.user).serialize();
    }

    public static SoknadTester startSoknad() throws Exception {
        return new SoknadTester().start();
    }

    @Override
    protected Application configure() {
        return new SoknadApplication();
    }

    private SoknadTester start() throws Exception {
        setUp();
        client().register(GsonProvider.class);

        Response response = sendsoknad().path("soknader/opprettSoknad")
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .header(JwtTokenConstants.AUTHORIZATION_HEADER, "Bearer " + token)
                .buildPost(null)
                .invoke();
        checkResponse(response, SC_OK);
        brukerBehandlingId = (String) response.readEntity(Map.class).get("brukerBehandlingId");
        return this;
    }

    private WebTarget sendsoknad() {
        return target("/sendsoknad/").queryParam("fnr", this.user);
    }

    private void checkResponse(Response invoke, int expectedStatusCode) {
        int actualStatusCode = invoke.getStatus();
        if (actualStatusCode != expectedStatusCode ){
            throw new WebApplicationException(actualStatusCode);
        }
    }

    public Invocation.Builder sendsoknadResource(String suburl, Function<WebTarget, WebTarget> webTargetDecorator) {
        WebTarget target = target("/sendsoknad/" + suburl);
        MediaType APPLICATION_PDF_TYPE = new MediaType("application", "pdf");
        return webTargetDecorator.apply(target)
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE, TEXT_PLAIN_TYPE, TEXT_HTML_TYPE, TEXT_XML_TYPE, APPLICATION_PDF_TYPE, WILDCARD_TYPE);
    }

    public String getBrukerBehandlingId() {
        return brukerBehandlingId;
    }

    public String getUser() {
        return user;
    }

}
