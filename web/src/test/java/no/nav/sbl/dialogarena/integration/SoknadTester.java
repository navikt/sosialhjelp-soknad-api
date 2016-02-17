package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.assertj.core.api.AbstractObjectAssert;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class SoknadTester extends JerseyTest {
    private final String skjema;
    private String brukerBehandlingId;
    private WebSoknad soknad;

    private SoknadTester(String soknad) {
        super();
        this.skjema = soknad;
    }

    public static SoknadTester startSoknad(String soknad) throws Exception {
        return new SoknadTester(soknad).start();
    }

    @Override
    protected Application configure() {
        return new SoknadApplication();
    }

    private SoknadTester start() throws Exception {
        setUp();
        client().register(GsonProvider.class);

        StartSoknad soknadType = new StartSoknad();
        soknadType.setSoknadType("INTEGRATION-1");
        Entity sokEntity = Entity.json(soknadType);
        Response invoke = target("/sendsoknad/soknader").request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("POST", sokEntity).invoke();
        checkResponse(invoke, SC_OK);
        brukerBehandlingId = (String) invoke.readEntity(Map.class).get("brukerBehandlingId");
        return this;
    }

    public SoknadTester settDelstegstatus(String status) {
        Response invoke = soknadResource().queryParam("delsteg", status).request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("PUT", Entity.json("")).invoke();
        checkResponse(invoke, SC_NO_CONTENT);
        System.out.println("Delstegstatus satt til " + status);
        return this;
    }

    private WebTarget soknadResource(String suburl) {
        return target("/sendsoknad/soknader/" + brukerBehandlingId + suburl);
    }

    private WebTarget soknadResource() {
        return soknadResource("");
    }

    private WebTarget faktumResource(Long faktumId) {
        return target("/sendsoknad/fakta/" + faktumId);
    }

    public SoknadTester hentSoknad() {
        Response response = soknadResource().request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("GET").invoke();
        soknad = response.readEntity(WebSoknad.class);
        checkResponse(response, SC_OK);
        return this;
    }


    public SoknadTester endreFaktum(Faktum faktum) {
        Response response = faktumResource(faktum.getFaktumId()).request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("PUT", Entity.json(faktum)).invoke();
        checkResponse(response, SC_OK);
        return this;
    }

    private void checkResponse(Response invoke, int status) {
        assertThat(invoke.getStatus()).isEqualTo(status);
    }

    public AbstractObjectAssert<?, WebSoknad> assertSoknad() {
        return assertThat(soknad);
    }

    public FaktumTester updateFaktum(String key) {
        return new FaktumTester(soknad.getFaktumMedKey(key));
    }

    public SoknadTester hentFakta() {
        Response response = soknadResource("/fakta").request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("GET").invoke();
        soknad.setFakta(response.readEntity(new GenericType<List<Faktum>>(){}));
        checkResponse(response, SC_OK);
        return this;
    }

    public SoknadTester print() {
        System.out.println(ToStringBuilder.reflectionToString(soknad, ToStringStyle.MULTI_LINE_STYLE));
        return this;
    }

    public VedleggTester hentPaakrevdeVedlegg() {
        Response response = soknadResource("/vedlegg").request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("GET").invoke();
        soknad.setVedlegg(response.readEntity(new GenericType<List<Vedlegg>>(){}));
        return new VedleggTester();
    }

    public SoknadTester sendInn() {
        Response response = soknadResource("/actions/send").request(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).build("POST").invoke();
        checkResponse(response, SC_NO_CONTENT);
        return this;
    }

    public class VedleggTester {
        public VedleggTester skalHaVedlegg(String... skjemanummer){
            assertThat(soknad.getVedlegg()).extracting("skjemaNummer").contains(skjemanummer);
            return this;
        }

        public SoknadTester soknad() {
            return SoknadTester.this;
        }
    }

    public class FaktumTester {

        private final Faktum faktum;

        private FaktumTester(Faktum faktumMedKey) {
            this.faktum = faktumMedKey;
        }

        public FaktumTester withValue(String value) {
            faktum.setValue(value);
            return this;
        }

        public SoknadTester utforEndring() {
            return endreFaktum(faktum);
        }
    }
}
