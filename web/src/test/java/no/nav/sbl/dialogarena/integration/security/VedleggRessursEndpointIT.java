package no.nav.sbl.dialogarena.integration.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;

@Ignore("Denne testen må skrives om slik at den virker med sosialhjelpsøknaden.")
public class VedleggRessursEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "10108000398";
    private static final String skjemanummer = SosialhjelpInformasjon.SKJEMANUMMER;

    @BeforeClass
    public static void beforeClass() throws Exception {
        beforeClass(true);
    }

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }


    @Test
    public void accessDeniedMedAnnenBruker_hentVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        Vedlegg testVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        }).get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId();

        Response responseMedAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        Response responseMedSammeBruker = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .buildGet()
                .invoke();

        assertThat(responseMedAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedSammeBruker.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }


    @Test
    public void accessDeniedMedAnnenBruker_lagreVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        Vedlegg testVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        }).get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId();

        Response responseMedAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                        webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildPut(Entity.json(testVedlegg))
                .invoke();

        Response responseMedSammeBruker = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .buildPut(Entity.json(testVedlegg))
                .invoke();

        Response responseMedAnnenBehandlingsId = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken("TEST2"))
                .buildPut(Entity.json(testVedlegg))
                .invoke();

        assertThat(responseMedAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedSammeBruker.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedAnnenBehandlingsId.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void accessDeniedMedAnnenBruker_slettVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        Vedlegg testVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        }).get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId();

        Response responseMedAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .buildDelete()
                .invoke();

        Response responseMedSammeBruker = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .buildDelete()
                .invoke();

        Response responseMedAnnenBehandlingsId = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken("TEST2"))
                .buildDelete()
                .invoke();

        assertThat(responseMedAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedSammeBruker.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedAnnenBehandlingsId.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void accessDeniedMedAnnenBruker_hentVedleggUnderBehandling() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        Vedlegg testVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        }).get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId() + "/fil";

        Response responseMedAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("behandlingsId", soknadTester.getBrukerBehandlingId()).queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        Response responseMedSammeBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("behandlingsId", soknadTester.getBrukerBehandlingId()))
                .buildGet()
                .invoke();

        assertThat(responseMedAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseMedSammeBruker.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }



    @Test
    public void accessDeniedMedAnnenBruker_lagForhandsvisningForVedlegg() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        Vedlegg testVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        }).get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId() + "/fil.png";

        Response responseMedAnnenBruker = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("side", 1).queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();

        Response responseUtenFnr = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("side", 1))
                .buildGet()
                .invoke();

        assertThat(responseMedAnnenBruker.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(responseUtenFnr.getStatus()).isNotEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    /*

    Starten på en sikkerhetstest for lastOppFiler. Denne testen feiler nå fordi man prøver å konvertere MultiPartFormDataen
    til json - dette kan fikses i GsonProvider-klassen (ville ikke committe den fiksen siden det ikke feiler for andre tester).
    Når det er gjort, vil man få 400 i stedet for 403, dvs. at det er noe i denne requesten som ikke er satt opp riktig, sannsynligvis
    MultipartFormData/files[]-delen. Har prøvd å sette accept og request headere i sendsoknadResource-metoden uten hell.

    @Test
    public void accessDeniedMedAnnenBruker_lastOppFiler() throws IOException {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett();

        List<Vedlegg> paakrevdeVedlegg = soknadTester.soknadResource("/vedlegg").build("GET").invoke().readEntity(new GenericType<List<Vedlegg>>() {
        });
        Vedlegg testVedlegg = paakrevdeVedlegg.get(0);
        String subUrl = "vedlegg/" + testVedlegg.getVedleggId() + "/fil";

        FormDataBodyPart fil = new FormDataBodyPart("files[]", "test.png");
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multiPart = (FormDataMultiPart) formDataMultiPart.field("testnavn", "test").bodyPart(fil);

        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("behandlingsId", soknadTester.getBrukerBehandlingId())
                        .queryParam("fnr", ANNEN_BRUKER))
                .header("X-XSRF-TOKEN", soknadTester.getXhrValue())
                .buildPost(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))
                .invoke();

        formDataMultiPart.close();
        multiPart.close();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

    }
    */

}
