package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.rest.SoknadApplication;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML;
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

    private Invocation.Builder alternativRepresentasjonResource() {
        WebTarget target = target("/sendsoknad/representasjon/xml/" + this.brukerBehandlingId);
        return target
                .request(TEXT_XML)
                .accept(TEXT_XML);
    }

    private Invocation.Builder faktumResource(Function<WebTarget, WebTarget> webTargetDecorator) {
        return webTargetDecorator.apply(target("/sendsoknad/fakta/"))
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE);
    }

    private Invocation.Builder vedleggResource(Function<WebTarget, WebTarget> webTargetDecorator) {
        return webTargetDecorator.apply(target("/sendsoknad/vedlegg/"))
                .request(APPLICATION_JSON_TYPE)
                .accept(APPLICATION_JSON_TYPE);
    }

    public SoknadTester hentSoknad() {
        Response response = soknadResource().build("GET").invoke();
        soknad = response.readEntity(WebSoknad.class);
        checkResponse(response, SC_OK);
        return this;
    }

    public Response hentAlternativRepresentasjonResponseMedStatus() {
        return alternativRepresentasjonResource().buildGet().invoke();
    }

    public <T> T hentAlternativRepresentasjon(Class<T> soeknadsskjemaEngangsstoenadClass) {
        Response response = hentAlternativRepresentasjonResponseMedStatus();
        return response.readEntity(soeknadsskjemaEngangsstoenadClass);
    }

    private SoknadTester endreFaktum(Faktum faktum) {
        String faktumId = faktum.getFaktumId().toString();
        Response response = faktumResource(webTarget -> webTarget.path(faktumId))
                .build("PUT", Entity.json(faktum))
                .invoke();
        checkResponse(response, SC_OK);
        return this;
    }

    private SoknadTester endreVedlegg(Vedlegg vedlegg) {
        String vedleggsId = vedlegg.getVedleggId().toString();
        Response response = vedleggResource(webTarget -> webTarget.path(vedleggsId))
                .build("PUT", Entity.json(vedlegg))
                .invoke();
        checkResponse(response, SC_NO_CONTENT);
        return this;
    }

    public FaktumOppretter nyttFaktum(String key) {
        return new FaktumOppretter(key);
    }

    private SoknadTester opprettFaktum(Faktum faktum) {
        faktumResource(webTarget -> webTarget.queryParam("behandlingsId", brukerBehandlingId))
                .buildPost(Entity.json(faktum))
                .invoke();
        hentFakta();
        return this;
    }

    private void checkResponse(Response invoke, int status) {
        assertThat(invoke.getStatus()).isEqualTo(status);
    }

    public AbstractObjectAssert<?, WebSoknad> assertSoknad() {
        return assertThat(soknad);
    }

    public FaktumTester faktum(String key) {
        List<Faktum> faktumMedKey = soknad.getFaktaMedKey(key);
        return new FaktumTester(single(faktumMedKey, key));
    }

    private static <T> T single(List<T> list, String identifier) {
        if (list.size() > 1) {
            throw new RuntimeException(String.format("Forventet bare å finne ett element for %s", identifier));
        } else if (list.isEmpty()) {
            throw new RuntimeException(String.format("Fant ingen elementer for %s", identifier));
        }
        return list.get(0);
    }

    public FaktaTester alleFaktum(String key) {
        List<FaktumTester> faktumTestere = soknad.getFaktaMedKey(key).stream().map(FaktumTester::new).collect(toList());
        return new FaktaTester(faktumTestere);
    }

    public SoknadTester hentFakta() {
        Response response = soknadResource("/fakta").build("GET").invoke();
        soknad.setFakta(response.readEntity(new GenericType<List<Faktum>>() {
        }));
        checkResponse(response, SC_OK);
        return this;
    }

    public SoknadTester print() {
        System.out.println(ToStringBuilder.reflectionToString(soknad, ToStringStyle.MULTI_LINE_STYLE));
        return this;
    }

    public VedleggTester hentPaakrevdeVedlegg() {
        Response response = soknadResource("/vedlegg").build("GET").invoke();
        soknad.setVedlegg(response.readEntity(new GenericType<List<Vedlegg>>() {
        }));
        return new VedleggTester();
    }

    public SoknadTester sendInn() {
        Response response = soknadResource("/actions/send").build("POST").invoke();
        checkResponse(response, SC_NO_CONTENT);
        return this;
    }

    public class VedleggTester {

        private Vedlegg vedlegg;

        public VedleggTester skalHaVedlegg(String... skjemanummer) {
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

        public VedleggTester vedlegg(Skjemanummer skjemanummer) {
            return vedlegg(skjemanummer.toString());
        }

        public VedleggTester vedlegg(String skjemaNummer) {
            List<Vedlegg> vedleggListe = soknad.getVedlegg().stream()
                    .filter(x -> x.getSkjemaNummer().equals(skjemaNummer))
                    .collect(toList());
            vedlegg = single(vedleggListe, skjemaNummer);
            return this;
        }

        public VedleggTester withInnsendingsValg(Vedlegg.Status innsendingsValg){
            vedlegg.setInnsendingsvalg(innsendingsValg);
            return this;
        }

        public SoknadTester utforEndring() {
            return endreVedlegg(vedlegg);
        }

        public VedleggTester withAarsak(String aarsak) {
            vedlegg.setAarsak(aarsak);
            return this;
        }
    }

    public class FaktumTester {

        private final Faktum faktum;
        private String value;
        private Map<String, String> properties = new HashMap<>();

        private FaktumTester(Faktum faktumMedKey) {
            this.faktum = faktumMedKey;
        }

        public FaktumTester withValue(String value) {
            this.value = value;
            return this;
        }

        public FaktumTester withProperties(Map<String, String> properties) {
            this.properties.putAll(properties);
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
            properties.forEach(faktum::medProperty);
            return endreFaktum(faktum);
        }

        FaktumTester withProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }
    }

    public class FaktaTester {
        private List<FaktumTester> faktumTestere;

        FaktaTester(List<FaktumTester> faktumTestere) {
            this.faktumTestere = faktumTestere;
        }

        FaktaTester skalVareSystemFaktum() {
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

    public class FaktumOppretter {

        private Faktum faktum;

        FaktumOppretter(String key) {
            this.faktum = new Faktum().medKey(key);
        }

        public FaktumOppretter withValue(String value) {
            faktum.medValue(value);
            return this;
        }

        public FaktumOppretter withParentFaktum(String parentKey) {
            Faktum parentFaktum = single(soknad.getFaktaMedKey(parentKey), parentKey);
            faktum.medParrentFaktumId(parentFaktum.getFaktumId());
            return this;
        }

        public SoknadTester opprett() {
            return SoknadTester.this.opprettFaktum(faktum);
        }

        public FaktumOppretter withProperty(String key, String value) {
            faktum.medProperty(key, value);
            return this;
        }

        public FaktumOppretter withProperties(Map<String, String> periodeProperties) {
            periodeProperties.forEach(faktum::medProperty);
            return this;
        }
    }
}
