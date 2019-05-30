package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Function;

import static javax.ws.rs.core.MediaType.*;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

public class SoknadTester extends JerseyTest {
    private String user;
    private String token;

    private String brukerBehandlingId;

    private SoknadUnderArbeid soknadUnderArbeid;
    private Pair<String, String> xhrHeader;
    private SoknadTester() {
        super();
        this.user = "01015245464";
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

        StartSoknad soknadType = new StartSoknad();
        soknadType.setSoknadType(SosialhjelpInformasjon.SKJEMANUMMER);
        Entity sokEntity = Entity.json(soknadType);
        Response response = sendsoknad().path("soknader")
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token)
                .buildPost(sokEntity)
                .invoke();
        checkResponse(response, SC_OK);
        brukerBehandlingId = (String) response.readEntity(Map.class).get("brukerBehandlingId");
        saveXhrValue(response.getCookies().get(SoknadRessurs.XSRF_TOKEN).getValue());
        return this;
    }

    private WebTarget sendsoknad() {
        return target("/sendsoknad/").queryParam("fnr", this.user);
    }

    private void saveXhrValue(String value){
        this.xhrHeader =  new ImmutablePair("X-XSRF-TOKEN", value);
    }

    public Invocation.Builder soknadResource(String suburl) {
        return soknadResource(suburl, Function.identity());
    }

    private Invocation.Builder soknadResource(String suburl, Function<WebTarget, WebTarget> webTargetDecorator) {
        WebTarget target = sendsoknad().path("soknader/").path(brukerBehandlingId).path(suburl);
        return webTargetDecorator.apply(target)
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + token);
    }


    private Invocation.Builder soknadResource() {
        return soknadResource("");
    }

    public SoknadTester hentSoknad() {
        Response response = soknadResource().build("GET").invoke();
        soknadUnderArbeid = response.readEntity(SoknadUnderArbeid.class);
        checkResponse(response, SC_OK);
        return this;
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

    public String getXhrHeader() {
        return xhrHeader.getValue();
    }

    public String getUser() {
        return user;
    }

}
