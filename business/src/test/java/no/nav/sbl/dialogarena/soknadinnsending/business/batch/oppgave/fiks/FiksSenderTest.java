package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.Brevtype;
import no.ks.svarut.servicesv9.Dokument;
import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.OrganisasjonDigitalAdresse;
import no.ks.svarut.servicesv9.PostAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.ETTERSENDELSE_TIL_NAV;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.SOKNAD_TIL_NAV;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FiksSenderTest {

    private static final String FIKSFORSENDELSE_ID = "6767";
    private static final String FILNAVN = "filnavn.jpg";
    private static final String ORGNUMMER = "9999";
    private static final String NAVENHETSNAVN = "NAV Sagene";
    private static final String BEHANDLINGSID = "12345";
    private static final String EIER = "12345678910";
    @Mock
    ForsendelsesServiceV9 forsendelsesService;
    @Mock
    DokumentKrypterer dokumentKrypterer;
    @Mock
    InnsendingService innsendingService;
    @Mock
    SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @InjectMocks
    private FiksSender fiksSender;

    private static final PostAdresse FAKE_ADRESSE = new PostAdresse()
            .withNavn(NAVENHETSNAVN)
            .withPostnr("0000")
            .withPoststed("Ikke send");

    @Before
    public void setUp() {
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withFiksforsendelseId(FIKSFORSENDELSE_ID));
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid());
        when(sosialhjelpPdfGenerator.generate(any(JsonInternalSoknad.class), anyBoolean())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateEttersendelsePdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()).thenReturn(new byte[]{1, 2, 3});

        setProperty(FiksSender.KRYPTERING_DISABLED, "");
        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);
    }

    @Test
    public void opprettForsendelseSetterRiktigInfoPaForsendelsenMedKryptering() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, FAKE_ADRESSE);

        OrganisasjonDigitalAdresse adresse = (OrganisasjonDigitalAdresse) forsendelse.getMottaker().getDigitalAdresse();
        assertThat(adresse.getOrgnr(), is(ORGNUMMER));
        assertThat(forsendelse.getMottaker().getPostAdresse().getNavn(), is(NAVENHETSNAVN));
        assertThat(forsendelse.getAvgivendeSystem(), is("digisos_avsender"));
        assertThat(forsendelse.getForsendelseType(), is("nav.digisos"));
        assertThat(forsendelse.getEksternref(), is(BEHANDLINGSID));
        assertThat(forsendelse.isKunDigitalLevering(), is(false));
        assertThat(forsendelse.getPrintkonfigurasjon().getBrevtype(), is(Brevtype.APOST));
        assertThat(forsendelse.isKryptert(), is(true));
        assertThat(forsendelse.isKrevNiva4Innlogging(), is(true));
        assertThat(forsendelse.getSvarPaForsendelse(), nullValue());
        assertThat(forsendelse.getDokumenter().size(), is(5));
        assertThat(forsendelse.getMetadataFraAvleverendeSystem().getDokumentetsDato(), notNullValue());
        verify(dokumentKrypterer, times(5)).krypterData(any());
    }

    @Test
    public void opprettForsendelseSetterRiktigInfoPaForsendelsenUtenKryptering() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        setProperty(FiksSender.KRYPTERING_DISABLED, "true");
        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, FAKE_ADRESSE);

        assertThat(forsendelse.isKryptert(), is(false));
        assertThat(forsendelse.isKrevNiva4Innlogging(), is(false));
        verify(dokumentKrypterer, never()).krypterData(any());
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForNySoknad() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel(), is(SOKNAD_TIL_NAV));
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForEttersendelse() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("12345")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending())
                .withEier(EIER));
        //when(any(SoknadUnderArbeid.class).getJsonInternalSoknad()).thenReturn(lagInternalSoknadForEttersending());
        SendtSoknad sendtSoknad = lagSendtSoknad().withTilknyttetBehandlingsId("12345");

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel(), is(ETTERSENDELSE_TIL_NAV));
    }

    @Test(expected = IllegalStateException.class)
    public void opprettForsendelseForEttersendelseUtenSvarPaForsendelseSkalFeile() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString()))
                .thenReturn(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER));
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withFiksforsendelseId(null));
        SendtSoknad sendtEttersendelse = lagSendtEttersendelse();

        fiksSender.opprettForsendelse(sendtEttersendelse, new PostAdresse());
    }

    @Test
    public void hentDokumenterFraSoknadReturnererFireDokumenterForSoknadUtenVedlegg() {
        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        assertThat(fiksDokumenter.size(), is(5));
        assertThat(fiksDokumenter.get(0).getFilnavn(), is("soknad.json"));
        assertThat(fiksDokumenter.get(1).getFilnavn(), is("Soknad.pdf"));
        assertThat(fiksDokumenter.get(2).getFilnavn(), is("vedlegg.json"));
        assertThat(fiksDokumenter.get(3).getFilnavn(), is("Soknad-juridisk.pdf"));
        assertThat(fiksDokumenter.get(4).getFilnavn(), is("Brukerkvittering.pdf"));
    }

    @Test
    public void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("123")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending()));

        assertThat(fiksDokumenter.size(), is(4));
        assertThat(fiksDokumenter.get(0).getFilnavn(), is("ettersendelse.pdf"));
        assertThat(fiksDokumenter.get(1).getFilnavn(), is("vedlegg.json"));
        assertThat(fiksDokumenter.get(2).getFilnavn(), is("Brukerkvittering.pdf"));
        assertThat(fiksDokumenter.get(3).getFilnavn(), is(FILNAVN));
    }

    @Test(expected = RuntimeException.class)
    public void hentDokumenterFraSoknadKasterFeilHvisSoknadManglerForNySoknad() {
        fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid());
    }

    @Test(expected = RuntimeException.class)
    public void hentDokumenterFraSoknadKasterFeilHvisVedleggManglerForEttersending() {
        fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid().withTilknyttetBehandlingsId("123"));
    }

    @After
    public void tearDown() {
        clearProperty(FiksSender.KRYPTERING_DISABLED);
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