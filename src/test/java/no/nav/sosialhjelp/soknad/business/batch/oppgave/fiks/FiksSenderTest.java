package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.ks.fiks.svarut.klient.model.Digitaladresse;
import no.ks.fiks.svarut.klient.model.Dokument;
import no.ks.fiks.svarut.klient.model.Forsendelse;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.client.svarut.SvarUtService;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksSender.ETTERSENDELSE_TIL_NAV;
import static no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksSender.SOKNAD_TIL_NAV;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class FiksSenderTest {

    private static final String FIKSFORSENDELSE_ID = UUID.randomUUID().toString();
    private static final String FILNAVN = "filnavn.jpg";
    private static final String ORGNUMMER = "9999";
    private static final String NAVENHETSNAVN = "NAV Sagene";
    private static final String BEHANDLINGSID = "12345";
    private static final String EIER = "12345678910";
    @Mock
    private DokumentKrypterer dokumentKrypterer;
    @Mock
    private InnsendingService innsendingService;
    @Mock
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;
    @Mock
    private SvarUtService svarUtService;

    private FiksSender fiksSender;

    @BeforeEach
    public void setUp() {
        System.clearProperty("environment.name");
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class)))
                .thenReturn(new SendtSoknad().withFiksforsendelseId(FIKSFORSENDELSE_ID));
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid());
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateEttersendelsePdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});

        fiksSender = new FiksSender(dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, true, svarUtService);
    }

    @Test
    void createForsendelseSetterRiktigInfoPaForsendelsenMedKryptering() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        Forsendelse forsendelse = fiksSender.createForsendelse(sendtSoknad, filnavnInputStreamMap);

        Digitaladresse adresse = forsendelse.getMottaker().getDigitalAdresse();
        assertThat(adresse.getOrganisasjonsNummer()).isEqualTo(ORGNUMMER);
        assertThat(forsendelse.getMottaker().getPostAdresse().getNavn()).isEqualTo(NAVENHETSNAVN);
        assertThat(forsendelse.getAvgivendeSystem()).isEqualTo("digisos_avsender");
        assertThat(forsendelse.getForsendelsesType()).isEqualTo("nav.digisos");
        assertThat(forsendelse.getEksternReferanse()).isEqualTo(BEHANDLINGSID);
        assertThat(forsendelse.isKunDigitalLevering()).isFalse();
        assertThat(forsendelse.getUtskriftsKonfigurasjon().isTosidig()).isTrue();
        assertThat(forsendelse.isKryptert()).isTrue();
        assertThat(forsendelse.isKrevNiva4Innlogging()).isTrue();
        assertThat(forsendelse.getSvarPaForsendelse()).isNull();
        assertThat(forsendelse.getDokumenter()).hasSize(5);
        assertThat(forsendelse.getMetadataFraAvleverendeSystem().getDokumentetsDato()).isNotNull();
        verify(dokumentKrypterer, times(5)).krypterData(any());
    }

    @Test
    void opprettForsendelseSetterRiktigInfoPaForsendelsenUtenKryptering() {
        fiksSender = new FiksSender(dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, false, svarUtService);

        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        Forsendelse forsendelse = fiksSender.createForsendelse(sendtSoknad, filnavnInputStreamMap);

        assertThat(forsendelse.isKryptert()).isFalse();
        assertThat(forsendelse.isKrevNiva4Innlogging()).isFalse();
        verify(dokumentKrypterer, never()).krypterData(any());
    }

    @Test
    void createForsendelseSetterRiktigTittelForNySoknad() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        Forsendelse forsendelse = fiksSender.createForsendelse(sendtSoknad, filnavnInputStreamMap);

        assertThat(forsendelse.getTittel()).isEqualTo(SOKNAD_TIL_NAV);
    }

    @Test
    void createForsendelseSetterRiktigTittelForEttersendelse() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("12345")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending())
                .withEier(EIER));
        //when(any(SoknadUnderArbeid.class).getJsonInternalSoknad()).thenReturn(lagInternalSoknadForEttersending());
        SendtSoknad sendtSoknad = lagSendtSoknad().withTilknyttetBehandlingsId("12345");

        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        Forsendelse forsendelse = fiksSender.createForsendelse(sendtSoknad, filnavnInputStreamMap);

        assertThat(forsendelse.getTittel()).isEqualTo(ETTERSENDELSE_TIL_NAV);
    }

    @Test
    void opprettForsendelseForEttersendelseUtenSvarPaForsendelseSkalFeile() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withFiksforsendelseId(null));
        SendtSoknad sendtEttersendelse = lagSendtEttersendelse();

        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> fiksSender.createForsendelse(sendtEttersendelse, filnavnInputStreamMap));
    }

    @Test
    void hentDokumenterFraSoknadReturnererFireDokumenterForSoknadUtenVedlegg() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(
                new SoknadUnderArbeid()
                        .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)),
                filnavnInputStreamMap);

        assertThat(fiksDokumenter).hasSize(5);
        assertThat(fiksDokumenter.get(0).getFilnavn()).isEqualTo("soknad.json");
        assertThat(fiksDokumenter.get(1).getFilnavn()).isEqualTo("Soknad.pdf");
        assertThat(fiksDokumenter.get(2).getFilnavn()).isEqualTo("vedlegg.json");
        assertThat(fiksDokumenter.get(3).getFilnavn()).isEqualTo("Soknad-juridisk.pdf");
        assertThat(fiksDokumenter.get(4).getFilnavn()).isEqualTo("Brukerkvittering.pdf");
    }

    @Test
    void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(
                new SoknadUnderArbeid()
                    .withTilknyttetBehandlingsId("123")
                    .withJsonInternalSoknad(lagInternalSoknadForEttersending()),
                filnavnInputStreamMap);

        assertThat(fiksDokumenter).hasSize(4);
        assertThat(fiksDokumenter.get(0).getFilnavn()).isEqualTo("ettersendelse.pdf");
        assertThat(fiksDokumenter.get(1).getFilnavn()).isEqualTo("vedlegg.json");
        assertThat(fiksDokumenter.get(2).getFilnavn()).isEqualTo("Brukerkvittering.pdf");
        assertThat(fiksDokumenter.get(3).getFilnavn()).isEqualTo(FILNAVN);
    }

    @Test
    void hentDokumenterFraSoknadKasterFeilHvisSoknadManglerForNySoknad() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid(), filnavnInputStreamMap));
    }

    @Test
    void hentDokumenterFraSoknadKasterFeilHvisVedleggManglerForEttersending() {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid().withTilknyttetBehandlingsId("123"), filnavnInputStreamMap));
    }

    private JsonInternalSoknad lagInternalSoknadForEttersending() {
        List<JsonFiler> jsonFiler = new ArrayList<>();
        jsonFiler.add(new JsonFiler().withFilnavn(FILNAVN).withSha512("sha512"));
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg))
                .withSoknad(new JsonSoknad()
                        .withDriftsinformasjon(new JsonDriftsinformasjon())
                        .withData(new JsonData()
                                .withOkonomi(new JsonOkonomi()
                                .withOpplysninger(new JsonOkonomiopplysninger()))));
    }

    private List<OpplastetVedlegg> lagOpplastetVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(FILNAVN)
                .withSha512("sha512")
                .withVedleggType(new VedleggType("type|tilleggsinfo"))
                .withData(new byte[]{1, 2, 3}));
        return opplastedeVedlegg;
    }

    private SendtSoknad lagSendtSoknad() {
        return new SendtSoknad()
                .withBehandlingsId(BEHANDLINGSID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withEier(EIER)
                .withBrukerFerdigDato(LocalDateTime.now());
    }

    private SendtSoknad lagSendtEttersendelse() {
        return lagSendtSoknad().withTilknyttetBehandlingsId("soknadId");
    }
}