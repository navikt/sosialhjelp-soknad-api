package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.groups.Tuple;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class SoknadTester extends JerseyTest {
    private final String skjemaNummer;
    private String brukerBehandlingId;
    private WebSoknad soknad;

    private SoknadTester(String skjemaNummer) {
        super();
        this.skjemaNummer = skjemaNummer;
    }

    public static SoknadTester startSoknad(String skjemaNummer) throws Exception {
        return new SoknadTester(skjemaNummer).start();
    }

    @Override
    protected Application configure() {
        return new SoknadApplication();
    }

    private SoknadTester start() throws Exception {
        setUp();
        client().register(GsonProvider.class);

        StartSoknad soknadType = new StartSoknad();
        soknadType.setSoknadType(skjemaNummer);
        Entity sokEntity = Entity.json(soknadType);
        Response response = target("/sendsoknad/soknader")
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .buildPost(sokEntity)
                .invoke();
        checkResponse(response, SC_OK);
        brukerBehandlingId = (String) response.readEntity(Map.class).get("brukerBehandlingId");
        return this;
    }

    public SoknadTester settDelstegstatus(String status) {
        Response invoke = soknadResource("", webTarget -> webTarget.queryParam("delsteg", status))
                .buildPut(Entity.json(""))
                .invoke();
        checkResponse(invoke, SC_NO_CONTENT);
        System.out.println("Delstegstatus satt til " + status);
        return this;
    }

    private Invocation.Builder soknadResource(String suburl) {
        return soknadResource(suburl, Function.identity());
    }

    private Invocation.Builder soknadResource(String suburl, Function<WebTarget, WebTarget> webTargetDecorator) {
        WebTarget target = target("/sendsoknad/soknader/" + brukerBehandlingId + suburl);
        return webTargetDecorator.apply(target)
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE);
    }

    private Invocation.Builder soknadResource() {
        return soknadResource("");
    }

    private Invocation.Builder faktumResource(Function<WebTarget, WebTarget> webTargetDecorator) {
        return webTargetDecorator.apply(target("/sendsoknad/fakta/"))
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE);
    }

    public SoknadTester hentSoknad() {
        Response response = soknadResource().build("GET").invoke();
        soknad = response.readEntity(WebSoknad.class);
        checkResponse(response, SC_OK);
        return this;
    }


    public SoknadTester endreFaktum(Faktum faktum) {
        String faktumId = faktum.getFaktumId().toString();
        Response response = faktumResource(webTarget -> webTarget.path(faktumId))
                .build("PUT", Entity.json(faktum))
                .invoke();
        checkResponse(response, SC_OK);
        return this;
    }

    SoknadTester opprettFaktumWithValue(String key, String value) {
        return opprettFaktumWithValueAndProperties(key, value, emptyMap());
    }

    SoknadTester opprettFaktumWithValueAndParent(String key, String value, String parentKey) {
        List<Faktum> faktaMedKey = soknad.getFaktaMedKey(parentKey);
        Faktum faktum = new Faktum().medKey(key).medValue(value);
        Faktum parentFaktum = faktaMedKey.get(0);
        faktum.setParrentFaktum(parentFaktum.getFaktumId());
        faktumResource(webTarget -> webTarget.queryParam("behandlingsId", brukerBehandlingId))
                .buildPost(Entity.json(faktum))
                .invoke();
        return hentFakta();
    }

    SoknadTester opprettFaktumWithValueAndProperties(String key, String value, Map<String, String> properties) {
        Faktum faktum = new Faktum().medKey(key).medValue(value);
        properties.forEach(faktum::medProperty);
        faktumResource(webTarget -> webTarget.queryParam("behandlingsId", brukerBehandlingId))
                .buildPost(Entity.json(faktum))
                .invoke();
        return hentFakta();
    }

    private void checkResponse(Response invoke, int status) {
        assertThat(invoke.getStatus()).isEqualTo(status);
    }

    public AbstractObjectAssert<?, WebSoknad> assertSoknad() {
        return assertThat(soknad);
    }

    public FaktumTester faktum(String key) {
        List<Faktum> faktumMedKey = soknad.getFaktaMedKey(key);
        if (faktumMedKey.size() > 1) {
            throw new RuntimeException(String.format("Fant flere faktum for key [%s]", key));
        } else if (faktumMedKey.isEmpty()) {
            throw new RuntimeException(String.format("Fant ingen faktum for key [%s]", key));
        }
        return new FaktumTester(faktumMedKey.get(0));
    }

    public FaktaTester alleFaktum(String key) {
        List<FaktumTester> faktumTestere = soknad.getFaktaMedKey(key).stream().map(FaktumTester::new).collect(toList());
        return new FaktaTester(faktumTestere);
    }

    public SoknadTester hentFakta() {
        Response response = soknadResource("/fakta").build("GET").invoke();
        soknad.setFakta(response.readEntity(new GenericType<List<Faktum>>(){}));
        checkResponse(response, SC_OK);
        return this;
    }

    public SoknadTester print() {
        System.out.println(ToStringBuilder.reflectionToString(soknad, ToStringStyle.MULTI_LINE_STYLE));
        return this;
    }

    public VedleggTester hentPaakrevdeVedlegg() {
        Response response = soknadResource("/vedlegg").build("GET").invoke();
        soknad.setVedlegg(response.readEntity(new GenericType<List<Vedlegg>>(){}));
        return new VedleggTester();
    }

    public SoknadTester sendInn() {
        Response response = soknadResource("/actions/send").build("POST").invoke();
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

        public VedleggTester skalHaVedleggMedSkjemaNummerTillegg(String skjemaNummer, String skjemaNummerTillegg) {
            Tuple tuple = tuple(skjemaNummer, skjemaNummerTillegg);
            assertThat(soknad.getVedlegg()).extracting("skjemaNummer", "skjemanummerTillegg").contains(tuple);
            return this;
        }

        public VedleggTester skalIkkeHaVedlegg(String... skjemaNummer) {
            assertThat(soknad.getVedlegg()).extracting("skjemaNummer").doesNotContain(skjemaNummer);
            return this;
        }

        public VedleggTester skalIkkeKreveNoenVedlegg() {
            assertThat(soknad.getVedlegg()).isEmpty();
            return this;
        }
    }

    public class FaktumTester {

        private final Faktum faktum;
        private String value;

        private FaktumTester(Faktum faktumMedKey) {
            this.faktum = faktumMedKey;
        }

        public FaktumTester withValue(String value) {
            this.value = value;
            return this;
        }

        FaktumTester skalVareSystemFaktum() {
            assertThat(faktum.getType()).isEqualTo(SYSTEMREGISTRERT);
            return this;
        }

        SoknadTester utforEndring() {
            if (Objects.isNull(value)) {
                throw new RuntimeException("Ingen endring å utføre  - ingen value er satt.");
            }
            faktum.setValue(value);
            return endreFaktum(faktum);
        }

    }

    public class FaktaTester {
        private List<FaktumTester> faktumTestere;

        FaktaTester(List<FaktumTester> faktumTestere) {
            this.faktumTestere = faktumTestere;
        }

        FaktaTester skalVareSystemFaktum(){
            faktumTestere.forEach(FaktumTester::skalVareSystemFaktum);
            return this;
        }

        FaktaTester skalVareAntall(int antall) {
            assertThat(faktumTestere).hasSize(antall);
            return this;
        }

        SoknadTester soknad() {
            return SoknadTester.this;
        }

    }
}
