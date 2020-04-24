package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.Dokument;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import org.apache.cxf.attachment.ByteDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FiksDokumentHelperTest {
    private static final String FILNAVN = "vedlegg.pdf";
    private static final String ANNET_FILNAVN = "annetVedlegg.jpg";
    private static final String SHA512 = "sha512";
    private static final String ANNEN_SHA512 = "annensha512";
    private static final String TREDJE_FILNAVN = "tredjeVedlegg.jpg";
    private static final String TREDJE_SHA512 = "tredjesha512";
    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";
    private static final String TYPE2 = "dokumentasjon";
    private static final String TILLEGGSINFO2 = "aksjer";
    private static final String EIER = "12345678910";
    private static final byte[] DATA = {1, 2, 3};

    private DokumentKrypterer dokumentKrypterer = mock(DokumentKrypterer.class);
    private InnsendingService innsendingService = mock(InnsendingService.class);
    private PDFService pdfService = mock(PDFService.class);
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator = mock(SosialhjelpPdfGenerator.class);

    private FiksDokumentHelper fiksDokumentHelper;

    @Before
    public void setup() {
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastedeVedlegg());
        when(pdfService.genererSaksbehandlerPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererJuridiskPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererEttersendelsePdf(any(JsonInternalSoknad.class), anyString(), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        fiksDokumentHelper = new FiksDokumentHelper(false, dokumentKrypterer, innsendingService, pdfService, sosialhjelpPdfGenerator);
    }

    @Test
    public void lagDokumentForSoknadJsonLagerKorrektDokument() {
        Dokument soknadJson = fiksDokumentHelper.lagDokumentForSoknadJson(createEmptyJsonInternalSoknad(EIER));

        assertThat(soknadJson.getFilnavn(), is("soknad.json"));
        assertThat(soknadJson.getMimetype(), is("application/json"));
        assertThat(soknadJson.isEkskluderesFraPrint(), is(true));
        assertThat(soknadJson.getData(), notNullValue());
    }

    @Test
    public void lagDokumentForVedleggJsonLagerKorrektDokument() {
        Dokument vedleggJson = fiksDokumentHelper.lagDokumentForVedleggJson(lagInternalSoknadForVedlegg());

        assertThat(vedleggJson.getFilnavn(), is("vedlegg.json"));
        assertThat(vedleggJson.getMimetype(), is("application/json"));
        assertThat(vedleggJson.isEkskluderesFraPrint(), is(true));
        assertThat(vedleggJson.getData(), notNullValue());
    }

    @Test
    public void lagDokumentForSaksbehandlerPdfLagerKorrektDokument() {
        Dokument saksbehandlerPdf = fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(createEmptyJsonInternalSoknad(EIER));

        assertThat(saksbehandlerPdf.getFilnavn(), is("Soknad.pdf"));
        assertThat(saksbehandlerPdf.getMimetype(), is("application/pdf"));
        assertThat(saksbehandlerPdf.isEkskluderesFraPrint(), is(false));
        assertThat(saksbehandlerPdf.getData(), notNullValue());
    }

    @Test
    public void lagDokumentForJuridiskPdfLagerKorrektDokument() {
        Dokument juridiskPdf = fiksDokumentHelper.lagDokumentForJuridiskPdf(createEmptyJsonInternalSoknad(EIER));

        assertThat(juridiskPdf.getFilnavn(), is("Soknad-juridisk.pdf"));
        assertThat(juridiskPdf.getMimetype(), is("application/pdf"));
        assertThat(juridiskPdf.isEkskluderesFraPrint(), is(false));
        assertThat(juridiskPdf.getData(), notNullValue());
    }

    @Test
    public void lagDokumentForEttersendelsePdfLagerKorrektDokument() {
        Dokument ettersendelsePdf = fiksDokumentHelper.lagDokumentForEttersendelsePdf(createEmptyJsonInternalSoknad(EIER), EIER);

        assertThat(ettersendelsePdf.getFilnavn(), is("ettersendelse.pdf"));
        assertThat(ettersendelsePdf.getMimetype(), is("application/pdf"));
        assertThat(ettersendelsePdf.isEkskluderesFraPrint(), is(false));
        assertThat(ettersendelsePdf.getData(), notNullValue());
    }

    @Test
    public void lagDokumentListeForVedleggReturnererRiktigeVedlegg() {
        List<Dokument> dokumenter = fiksDokumentHelper.lagDokumentListeForVedlegg(new SoknadUnderArbeid());

        assertThat(dokumenter.size(), is(3));
        assertThat(dokumenter.get(0).getFilnavn(), is(FILNAVN));
        assertThat(dokumenter.get(1).getFilnavn(), is(ANNET_FILNAVN));
        assertThat(dokumenter.get(2).getFilnavn(), is(TREDJE_FILNAVN));
    }

    @Test
    public void opprettDokumentForVedleggOppretterDokumentKorrekt() {
        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg().withFilnavn(FILNAVN).withData(DATA);

        Dokument dokument = fiksDokumentHelper.opprettDokumentForVedlegg(opplastetVedlegg);

        assertThat(dokument.getFilnavn(), is(FILNAVN));
        assertThat(dokument.getData(), notNullValue());
        assertThat(dokument.getMimetype(), is("application/octet-stream"));
        assertThat(dokument.isEkskluderesFraPrint(), is(true));
    }

    @Test
    public void krypterOgOpprettByteDatasourceKryptererHvisSkalKryptereErTrue() {
        fiksDokumentHelper = new FiksDokumentHelper(true, dokumentKrypterer, innsendingService, pdfService, sosialhjelpPdfGenerator);

        ByteDataSource dataSource = fiksDokumentHelper.krypterOgOpprettByteDatasource(FILNAVN, DATA);

        assertThat(dataSource.getName(), is(FILNAVN));
        assertThat(dataSource.getContentType(), is("application/octet-stream"));
        assertThat(dataSource.getData()[0], is((byte) 3));
    }

    @Test
    public void krypterOgOpprettByteDatasourceKryptererIkkeHvisSkalKryptereErFalse() {
        ByteDataSource dataSource = fiksDokumentHelper.krypterOgOpprettByteDatasource(FILNAVN, DATA);

        assertThat(dataSource.getData()[0], is((byte) 1));
    }

    private List<OpplastetVedlegg> lagOpplastedeVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(FILNAVN)
                .withSha512(SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE + "|" + TILLEGGSINFO)));
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(ANNET_FILNAVN)
                .withSha512(ANNEN_SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE2 + "|" + TILLEGGSINFO2)));
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(TREDJE_FILNAVN)
                .withSha512(TREDJE_SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE2 + "|" + TILLEGGSINFO2)));
        return opplastedeVedlegg;
    }

    private JsonInternalSoknad lagInternalSoknadForVedlegg() {
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.VedleggKreves.name())
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO2));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO)
                .withFiler(lagJsonFiler(FILNAVN, SHA512)));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType(TYPE2)
                .withTilleggsinfo(TILLEGGSINFO2)
                .withFiler(lagJsonFilerMedToFiler(ANNET_FILNAVN, ANNEN_SHA512, TREDJE_FILNAVN, TREDJE_SHA512)));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon()
                        .withVedlegg(jsonVedlegg));
    }

    private List<JsonFiler> lagJsonFiler(String filnavn, String sha512) {
        List<JsonFiler> filer = new ArrayList<>();
        filer.add(new JsonFiler()
                .withFilnavn(filnavn)
                .withSha512(sha512));
        return filer;
    }

    private List<JsonFiler> lagJsonFilerMedToFiler(String filnavn, String sha, String filnavn2, String sha2) {
        List<JsonFiler> jsonFiler = lagJsonFiler(filnavn, sha);
        jsonFiler.add(new JsonFiler()
                .withFilnavn(filnavn2)
                .withSha512(sha2));
        return jsonFiler;
    }
}