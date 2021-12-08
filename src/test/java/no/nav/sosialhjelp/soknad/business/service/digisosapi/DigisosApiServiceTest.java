package no.nav.sosialhjelp.soknad.business.service.digisosapi;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilMetadata;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.innsending.InnsendingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.APPLICATION_PDF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigisosApiServiceTest {

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

    @BeforeEach
    public void setUpBefore() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void skalLageOpplastingsListeMedDokumenterForSoknad() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910")).withEier("eier");

        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});

        List<FilOpplasting> filOpplastings = digisosApiService.lagDokumentListe(soknadUnderArbeid);

        FilMetadata metadataFil1 = filOpplastings.get(0).metadata;
        assertThat(metadataFil1.filnavn).isEqualTo("Soknad.pdf");
        assertThat(metadataFil1.mimetype).isEqualTo(APPLICATION_PDF);


        FilMetadata metadataFil3 = filOpplastings.get(1).metadata;
        assertThat(metadataFil3.filnavn).isEqualTo("Soknad-juridisk.pdf");
        assertThat(metadataFil3.mimetype).isEqualTo(APPLICATION_PDF);

        FilMetadata metadataFil4 = filOpplastings.get(2).metadata;
        assertThat(metadataFil4.filnavn).isEqualTo("Brukerkvittering.pdf");
        assertThat(metadataFil4.mimetype).isEqualTo(APPLICATION_PDF);
    }

    @Test
    void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());
        when(sosialhjelpPdfGenerator.generateEttersendelsePdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});

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
    void getTilleggsinformasjonJson() {
        JsonSoknad soknad = new JsonSoknad().withMottaker(new JsonSoknadsmottaker().withEnhetsnummer("1234"));
        String tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad);
        assertThat(tilleggsinformasjonJson).isEqualTo("{\"enhetsnummer\":\"1234\"}");
    }

    @Test
    void getTilleggsinformasjonJson_withNoEnhetsnummer_shouldSetEnhetsnummerToNull() {
        JsonSoknad soknad = new JsonSoknad().withMottaker(new JsonSoknadsmottaker());
        String tilleggsinformasjonJson = digisosApiService.getTilleggsinformasjonJson(soknad);
        assertThat(tilleggsinformasjonJson).isEqualTo("{}");
    }

    @Test
    void getTilleggsinformasjonJson_withNoMottaker_shouldThrowException() {
        JsonSoknad soknad = new JsonSoknad();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> digisosApiService.getTilleggsinformasjonJson(soknad));
    }

    @Test
    void etterInnsendingSkalSoknadUnderArbeidSlettes() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910")).withEier("eier");

        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});

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
