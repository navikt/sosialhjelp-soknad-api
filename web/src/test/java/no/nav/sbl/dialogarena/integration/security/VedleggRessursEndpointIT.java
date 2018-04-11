package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VedleggRessursEndpointIT extends AbstractSecurityIT {

    private static final String ANNEN_BRUKER = "10108000398";
    private static final String skjemanummer = new DagpengerOrdinaerInformasjon().getSkjemanummer().get(0);

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
