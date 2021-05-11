package no.nav.sosialhjelp.soknad.business.service.digisosapi;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.SoknadServiceIntegrationTestContext;
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.digisosapi.FilMetadata;
import no.nav.sosialhjelp.soknad.domain.model.digisosapi.FilOpplasting;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = SoknadServiceIntegrationTestContext.class)
public class DigisosApiServiceTest {

    @Mock
    private InnsendingService innsendingService;
    @Mock
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;
    @Mock
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Mock
    private HenvendelseService henvendelseService;
    @Mock
    private DigisosApi digisosApi;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private DigisosApiService digisosApiService;

    @Before
    public void setUpBefore() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());

        System.setProperty("idporten_config_url", "https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration");
        System.setProperty("idporten_scope", "ks:fiks");
        System.setProperty("idporten_clientid", "1c3631f4-dbf2-4c12-bdc4-156cbd53c625");
        System.setProperty("idporten_token_url", "https://oidc-ver2.difi.no/idporten-oidc-provider/token");
        System.setProperty("digisos_api_baseurl", "https://api.fiks.test.ks.no/");
        System.setProperty("integrasjonsid_fiks", "c4bf2682-327f-4535-a087-c248d35978e1");

        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateEttersendelsePdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");

        System.clearProperty("idporten_config_url");
        System.clearProperty("idporten_scope");
        System.clearProperty("idporten_clientid");
        System.clearProperty("idporten_token_url");
        System.clearProperty("digisos_api_baseurl");
        System.clearProperty("integrasjonsid_fiks");
    }

    @Test
    public void skalLageOpplastingsListeMedDokumenterForSoknad() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910")).withEier("eier");

        List<FilOpplasting> filOpplastings = digisosApiService.lagDokumentListe(soknadUnderArbeid);

        FilMetadata metadataFil1 = filOpplastings.get(0).metadata;
        assertThat(metadataFil1.filnavn).isEqualTo("Soknad.pdf");
        assertThat(metadataFil1.mimetype).isEqualTo("application/pdf");


        FilMetadata metadataFil3 = filOpplastings.get(1).metadata;
        assertThat(metadataFil3.filnavn).isEqualTo("Soknad-juridisk.pdf");
        assertThat(metadataFil3.mimetype).isEqualTo("application/pdf");

        FilMetadata metadataFil4 = filOpplastings.get(2).metadata;
        assertThat(metadataFil4.filnavn).isEqualTo("Brukerkvittering.pdf");
        assertThat(metadataFil4.mimetype).isEqualTo("application/pdf");
    }

    @Test
    public void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<FilOpplasting> fiksDokumenter = digisosApiService.lagDokumentListe(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("123")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending())
                .withEier("eier"));

        assertThat(fiksDokumenter.size()).isEqualTo(3);
        assertThat(fiksDokumenter.get(0).metadata.filnavn).isEqualTo("ettersendelse.pdf");
        assertThat(fiksDokumenter.get(1).metadata.filnavn).isEqualTo("Brukerkvittering.pdf");
        assertThat(fiksDokumenter.get(2).metadata.filnavn).isEqualTo("FILNAVN");
    }

    @Test
    public void getTilleggsinformasjonJson() {
        JsonSoknad soknad = new JsonSoknad().withMottaker(new JsonSoknadsmottaker().withEnhetsnummer("1234"));
        String tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad);
        assertThat(tilleggsinformasjonJson).isEqualTo("{\"enhetsnummer\":\"1234\"}");
    }

    @Test
    public void getTilleggsinformasjonJson_withNoEnhetsnummer_shouldSetEnhetsnummerToNull() {
        JsonSoknad soknad = new JsonSoknad().withMottaker(new JsonSoknadsmottaker());
        String tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad);
        assertThat(tilleggsinformasjonJson).isEqualTo("{}");
    }

    @Test(expected = IllegalStateException.class)
    public void getTilleggsinformasjonJson_withNoMottaker_shouldThrowException() {
        JsonSoknad soknad = new JsonSoknad();
        String tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad);
        assertThat(tilleggsinformasjonJson).isEqualTo("hei");
    }

    @Test
    public void etterInnsendingSkalSoknadUnderArbeidSlettes() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910")).withEier("eier");

        when(digisosApi.krypterOgLastOppFiler(anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString()))
                .thenReturn("digisosid");

        digisosApiService.sendSoknad(soknadUnderArbeid, "token", "0301");

        verify(soknadUnderArbeidRepository, times(1)).slettSoknad(any(), anyString());
    }

    private JsonInternalSoknad lagInternalSoknadForEttersending() {
        List<JsonFiler> jsonFiler = new ArrayList<>();
        jsonFiler.add(new JsonFiler().withFilnavn("FILNAVN").withSha512("sha512"));
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
    }


    private List<OpplastetVedlegg> lagOpplastetVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn("FILNAVN")
                .withSha512("sha512")
                .withVedleggType(new VedleggType("type|tilleggsinfo"))
                .withData(new byte[]{1, 2, 3}));
        return opplastedeVedlegg;
    }
}
