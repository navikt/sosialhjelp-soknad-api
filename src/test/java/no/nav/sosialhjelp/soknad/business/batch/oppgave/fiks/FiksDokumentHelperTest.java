package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.ks.fiks.svarut.klient.model.Dokument;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.APPLICATION_PDF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FiksDokumentHelperTest {
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
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator = mock(SosialhjelpPdfGenerator.class);

    private FiksDokumentHelper fiksDokumentHelper;

    @BeforeEach
    public void setup() {
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastedeVedlegg());
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateEttersendelsePdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});
        fiksDokumentHelper = new FiksDokumentHelper(false, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);
    }

    @Test
    void lagDokumentForSoknadJsonLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument soknadJson = fiksDokumentHelper.lagDokumentForSoknadJson(createEmptyJsonInternalSoknad(EIER), filnavnInputStreamMap);

        assertThat(soknadJson.getFilnavn()).isEqualTo("soknad.json");
        assertThat(soknadJson.getMimeType()).isEqualTo(APPLICATION_JSON);
        assertThat(soknadJson.isEkskluderesFraUtskrift()).isTrue();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("soknad.json")).isNotNull();
    }

    @Test
    void lagDokumentForVedleggJsonLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument vedleggJson = fiksDokumentHelper.lagDokumentForVedleggJson(lagInternalSoknadForVedlegg(), filnavnInputStreamMap);

        assertThat(vedleggJson.getFilnavn()).isEqualTo("vedlegg.json");
        assertThat(vedleggJson.getMimeType()).isEqualTo(APPLICATION_JSON);
        assertThat(vedleggJson.isEkskluderesFraUtskrift()).isTrue();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("vedlegg.json")).isNotNull();
    }

    @Test
    void lagDokumentForSaksbehandlerPdfLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument saksbehandlerPdf = fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(createEmptyJsonInternalSoknad(EIER), filnavnInputStreamMap);

        assertThat(saksbehandlerPdf.getFilnavn()).isEqualTo("Soknad.pdf");
        assertThat(saksbehandlerPdf.getMimeType()).isEqualTo(APPLICATION_PDF);
        assertThat(saksbehandlerPdf.isEkskluderesFraUtskrift()).isFalse();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("Soknad.pdf")).isNotNull();
    }

    @Test
    void lagDokumentForJuridiskPdfLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument juridiskPdf = fiksDokumentHelper.lagDokumentForJuridiskPdf(createEmptyJsonInternalSoknad(EIER), filnavnInputStreamMap);

        assertThat(juridiskPdf.getFilnavn()).isEqualTo("Soknad-juridisk.pdf");
        assertThat(juridiskPdf.getMimeType()).isEqualTo(APPLICATION_PDF);
        assertThat(juridiskPdf.isEkskluderesFraUtskrift()).isFalse();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("Soknad-juridisk.pdf")).isNotNull();
    }

    @Test
    void lagDokumentForBrukerkvitteringPdfLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument brukerkvitteringPdf = fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(filnavnInputStreamMap);

        assertThat(brukerkvitteringPdf.getFilnavn()).isEqualTo("Brukerkvittering.pdf");
        assertThat(brukerkvitteringPdf.getMimeType()).isEqualTo(APPLICATION_PDF);
        assertThat(brukerkvitteringPdf.isEkskluderesFraUtskrift()).isTrue();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("Brukerkvittering.pdf")).isNotNull();
    }

    @Test
    void lagDokumentForEttersendelsePdfLagerKorrektDokument() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        Dokument ettersendelsePdf = fiksDokumentHelper.lagDokumentForEttersendelsePdf(createEmptyJsonInternalSoknad(EIER), EIER, filnavnInputStreamMap);

        assertThat(ettersendelsePdf.getFilnavn()).isEqualTo("ettersendelse.pdf");
        assertThat(ettersendelsePdf.getMimeType()).isEqualTo(APPLICATION_PDF);
        assertThat(ettersendelsePdf.isEkskluderesFraUtskrift()).isFalse();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get("ettersendelse.pdf")).isNotNull();
    }

    @Test
    void lagDokumentListeForVedleggReturnererRiktigeVedlegg() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        List<Dokument> dokumenter = fiksDokumentHelper.lagDokumentListeForVedlegg(new SoknadUnderArbeid(), filnavnInputStreamMap);

        assertThat(dokumenter).hasSize(3);
        assertThat(dokumenter.get(0).getFilnavn()).isEqualTo(FILNAVN);
        assertThat(dokumenter.get(1).getFilnavn()).isEqualTo(ANNET_FILNAVN);
        assertThat(dokumenter.get(2).getFilnavn()).isEqualTo(TREDJE_FILNAVN);
    }

    @Test
    void opprettDokumentForVedleggOppretterDokumentKorrekt() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg().withFilnavn(FILNAVN).withData(DATA);

        Dokument dokument = fiksDokumentHelper.opprettDokumentForVedlegg(opplastetVedlegg, filnavnInputStreamMap);

        assertThat(dokument.getFilnavn()).isEqualTo(FILNAVN);
        assertThat(dokument.getMimeType()).isEqualTo("application/octet-stream");
        assertThat(dokument.isEkskluderesFraUtskrift()).isTrue();
        assertThat(filnavnInputStreamMap).hasSize(1);
        assertThat(filnavnInputStreamMap.get(FILNAVN)).isNotNull();
    }

    @Test
    void krypterOgOpprettByteDatasourceKryptererHvisSkalKryptereErTrue() {
        fiksDokumentHelper = new FiksDokumentHelper(true, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);

        ByteArrayInputStream byteArrayInputStream = fiksDokumentHelper.krypterOgOpprettByteArrayInputStream(DATA);

        assertThat(byteArrayInputStream.readAllBytes()[0]).isEqualTo((byte) 3);
    }

    @Test
    void krypterOgOpprettByteDatasourceKryptererIkkeHvisSkalKryptereErFalse() {
        ByteArrayInputStream byteArrayInputStream = fiksDokumentHelper.krypterOgOpprettByteArrayInputStream(DATA);

        assertThat(byteArrayInputStream.readAllBytes()[0]).isEqualTo((byte) 1);
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