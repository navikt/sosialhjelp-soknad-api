package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.finn.unleash.Unleash;
import no.ks.svarut.servicesv9.Brevtype;
import no.ks.svarut.servicesv9.Dokument;
import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.OrganisasjonDigitalAdresse;
import no.ks.svarut.servicesv9.PostAdresse;
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
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private static final String FIKSFORSENDELSE_ID = "6767";
    private static final String FILNAVN = "filnavn.jpg";
    private static final String ORGNUMMER = "9999";
    private static final String NAVENHETSNAVN = "NAV Sagene";
    private static final String BEHANDLINGSID = "12345";
    private static final String EIER = "12345678910";
    @Mock
    private ForsendelsesServiceV9 forsendelsesService;
    @Mock
    private DokumentKrypterer dokumentKrypterer;
    @Mock
    private InnsendingService innsendingService;
    @Mock
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;
    @Mock
    private SvarUtService svarUtService;
    @Mock
    private Unleash unleash;

    private FiksSender fiksSender;

    private static final PostAdresse FAKE_ADRESSE = new PostAdresse()
            .withNavn(NAVENHETSNAVN)
            .withPostnr("0000")
            .withPoststed("Ikke send");

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
        when(unleash.isEnabled(any())).thenReturn(false);

        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, true, svarUtService, unleash);
    }

    @Test
    void opprettForsendelseSetterRiktigInfoPaForsendelsenMedKryptering() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, FAKE_ADRESSE);

        OrganisasjonDigitalAdresse adresse = (OrganisasjonDigitalAdresse) forsendelse.getMottaker().getDigitalAdresse();
        assertThat(adresse.getOrgnr()).isEqualTo(ORGNUMMER);
        assertThat(forsendelse.getMottaker().getPostAdresse().getNavn()).isEqualTo(NAVENHETSNAVN);
        assertThat(forsendelse.getAvgivendeSystem()).isEqualTo("digisos_avsender");
        assertThat(forsendelse.getForsendelseType()).isEqualTo("nav.digisos");
        assertThat(forsendelse.getEksternref()).isEqualTo(BEHANDLINGSID);
        assertThat(forsendelse.isKunDigitalLevering()).isFalse();
        assertThat(forsendelse.getPrintkonfigurasjon().getBrevtype()).isEqualTo(Brevtype.APOST);
        assertThat(forsendelse.isKryptert()).isTrue();
        assertThat(forsendelse.isKrevNiva4Innlogging()).isTrue();
        assertThat(forsendelse.getSvarPaForsendelse()).isNull();
        assertThat(forsendelse.getDokumenter()).hasSize(5);
        assertThat(forsendelse.getMetadataFraAvleverendeSystem().getDokumentetsDato()).isNotNull();
        verify(dokumentKrypterer, times(5)).krypterData(any());
    }

    @Test
    void opprettForsendelseSetterRiktigInfoPaForsendelsenUtenKryptering() {
        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, false, svarUtService, unleash);

        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, FAKE_ADRESSE);

        assertThat(forsendelse.isKryptert()).isFalse();
        assertThat(forsendelse.isKrevNiva4Innlogging()).isFalse();
        verify(dokumentKrypterer, never()).krypterData(any());
    }

    @Test
    void opprettForsendelseSetterRiktigTittelForNySoknad() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel()).isEqualTo(SOKNAD_TIL_NAV);
    }

    @Test
    void opprettForsendelseSetterRiktigTittelForEttersendelse() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("12345")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending())
                .withEier(EIER));
        //when(any(SoknadUnderArbeid.class).getJsonInternalSoknad()).thenReturn(lagInternalSoknadForEttersending());
        SendtSoknad sendtSoknad = lagSendtSoknad().withTilknyttetBehandlingsId("12345");

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel()).isEqualTo(ETTERSENDELSE_TIL_NAV);
    }

    @Test
    void opprettForsendelseForEttersendelseUtenSvarPaForsendelseSkalFeile() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withFiksforsendelseId(null));
        SendtSoknad sendtEttersendelse = lagSendtEttersendelse();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> fiksSender.opprettForsendelse(sendtEttersendelse, new PostAdresse()));
    }

    @Test
    void hentDokumenterFraSoknadReturnererFireDokumenterForSoknadUtenVedlegg() {
        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        assertThat(fiksDokumenter).hasSize(5);
        assertThat(fiksDokumenter.get(0).getFilnavn()).isEqualTo("soknad.json");
        assertThat(fiksDokumenter.get(1).getFilnavn()).isEqualTo("Soknad.pdf");
        assertThat(fiksDokumenter.get(2).getFilnavn()).isEqualTo("vedlegg.json");
        assertThat(fiksDokumenter.get(3).getFilnavn()).isEqualTo("Soknad-juridisk.pdf");
        assertThat(fiksDokumenter.get(4).getFilnavn()).isEqualTo("Brukerkvittering.pdf");
    }

    @Test
    void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("123")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending()));

        assertThat(fiksDokumenter).hasSize(4);
        assertThat(fiksDokumenter.get(0).getFilnavn()).isEqualTo("ettersendelse.pdf");
        assertThat(fiksDokumenter.get(1).getFilnavn()).isEqualTo("vedlegg.json");
        assertThat(fiksDokumenter.get(2).getFilnavn()).isEqualTo("Brukerkvittering.pdf");
        assertThat(fiksDokumenter.get(3).getFilnavn()).isEqualTo(FILNAVN);
    }

    @Test
    void hentDokumenterFraSoknadKasterFeilHvisSoknadManglerForNySoknad() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid()));
    }

    @Test
    void hentDokumenterFraSoknadKasterFeilHvisVedleggManglerForEttersending() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid().withTilknyttetBehandlingsId("123")));
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