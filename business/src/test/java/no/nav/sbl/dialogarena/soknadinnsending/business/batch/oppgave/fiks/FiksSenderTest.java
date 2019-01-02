package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.*;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.*;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.*;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.*;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.ETTERSENDELSE_TIL_NAV;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.SOKNAD_TIL_NAV;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FiksSenderTest {

    private static final String FIKSFORSENDELSE_ID = "6767";
    private static final String FILNAVN = "filnavn.jpg";
    private static final String ORGNUMMER = "9999";
    private static final String NAVENHETSNAVN = "NAV Sagene";
    private static final String BEHANDLINGSID = "12345";
    @Mock
    ForsendelsesServiceV9 forsendelsesService;
    @Mock
    DokumentKrypterer dokumentKrypterer;
    @Mock
    InnsendingService innsendingService;
    @Mock
    PDFService pdfService;

    private FiksSender fiksSender;

    private static final PostAdresse FAKE_ADRESSE = new PostAdresse()
            .withNavn(NAVENHETSNAVN)
            .withPostnr("0000")
            .withPoststed("Ikke send");

    @Before
    public void setUp() {
        when(forsendelsesService.sendForsendelse(any())).thenReturn("id1234");
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.finnSendtSoknadForEttersendelse(any(SoknadUnderArbeid.class))).thenReturn(new SendtSoknad()
                .withFiksforsendelseId(FIKSFORSENDELSE_ID));
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid());
        when(pdfService.genererSaksbehandlerPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererJuridiskPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererBrukerkvitteringPdf(any(JsonInternalSoknad.class), anyString(), anyBoolean(), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererEttersendelsePdf(any(JsonInternalSoknad.class), anyString(), anyString())).thenReturn(new byte[]{1, 2, 3});

        setProperty(FiksSender.KRYPTERING_DISABLED, "");
        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, pdfService);
    }

    @Test
    public void opprettForsendelseSetterRiktigInfoPaForsendelsenMedKryptering() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknad());
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
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknad());
        setProperty(FiksSender.KRYPTERING_DISABLED, "true");
        fiksSender = new FiksSender(forsendelsesService, dokumentKrypterer, innsendingService, pdfService);
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, FAKE_ADRESSE);

        assertThat(forsendelse.isKryptert(), is(false));
        assertThat(forsendelse.isKrevNiva4Innlogging(), is(false));
        verify(dokumentKrypterer, never()).krypterData(any());
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForNySoknad() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknad());
        SendtSoknad sendtSoknad = lagSendtSoknad();

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel(), is(SOKNAD_TIL_NAV));
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForEttersendelse() {
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid().withTilknyttetBehandlingsId("12345"));
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknadForEttersending());
        SendtSoknad sendtSoknad = lagSendtSoknad().withTilknyttetBehandlingsId("12345");

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, new PostAdresse());

        assertThat(forsendelse.getTittel(), is(ETTERSENDELSE_TIL_NAV));
    }

    @Test
    public void hentDokumenterFraSoknadReturnererFireDokumenterForSoknadUtenVedlegg() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknad());

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid());

        assertThat(fiksDokumenter.size(), is(5));
        assertThat(fiksDokumenter.get(0).getFilnavn(), is("soknad.json"));
        assertThat(fiksDokumenter.get(1).getFilnavn(), is("Soknad.pdf"));
        assertThat(fiksDokumenter.get(2).getFilnavn(), is("vedlegg.json"));
        assertThat(fiksDokumenter.get(3).getFilnavn(), is("Soknad-juridisk.pdf"));
        assertThat(fiksDokumenter.get(4).getFilnavn(), is("Brukerkvittering.pdf"));
    }

    @Test
    public void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknadForEttersending());
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<Dokument> fiksDokumenter = fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid().withTilknyttetBehandlingsId("123"));

        assertThat(fiksDokumenter.size(), is(4));
        assertThat(fiksDokumenter.get(0).getFilnavn(), is("ettersendelse.pdf"));
        assertThat(fiksDokumenter.get(1).getFilnavn(), is("vedlegg.json"));
        assertThat(fiksDokumenter.get(2).getFilnavn(), is("Brukerkvittering.pdf"));
        assertThat(fiksDokumenter.get(3).getFilnavn(), is(FILNAVN));
    }

    @Test(expected = RuntimeException.class)
    public void hentDokumenterFraSoknadKasterFeilHvisSoknadManglerForNySoknad() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknadForEttersending());

        fiksSender.hentDokumenterFraSoknad(new SoknadUnderArbeid());
    }

    @Test(expected = RuntimeException.class)
    public void hentDokumenterFraSoknadKasterFeilHvisVedleggManglerForEttersending() {
        when(innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class))).thenReturn(lagInternalSoknadUtenVedleggSpesifikasjon());

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
                .withStatus(Vedlegg.Status.LastetOpp.name())
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
    }

    private List<OpplastetVedlegg> lagOpplastetVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(FILNAVN)
                .withSha512("sha512")
                .withVedleggType(new VedleggType("type", "tilleggsinfo"))
                .withData(new byte[]{1, 2, 3}));
        return opplastedeVedlegg;
    }

    private JsonInternalSoknad lagInternalSoknad() {
        return lagInternalSoknadUtenVedleggSpesifikasjon().withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(new ArrayList<>()));
    }

    private JsonInternalSoknad lagInternalSoknadUtenVedleggSpesifikasjon() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withVersion("1.0.0")
                        .withKompatibilitet(emptyList())
                        .withDriftsinformasjon("")
                        .withData(new JsonData()
                                .withArbeid(new JsonArbeid())
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withHvaSokesOm("")
                                        .withHvorforSoke(""))
                                .withBosituasjon(new JsonBosituasjon())
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt()))
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger())
                                        .withOversikt(new JsonOkonomioversikt()))
                                .withPersonalia(new JsonPersonalia()
                                        .withKontonummer(new JsonKontonummer()
                                                .withKilde(JsonKilde.BRUKER))
                                        .withNavn(new JsonSokernavn()
                                                .withFornavn("Fornavn")
                                                .withMellomnavn("")
                                                .withEtternavn("Etternavn")
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM))
                                        .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                .withVerdi("12345678910")
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)))
                                .withUtdanning(new JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER))));
    }

    private SendtSoknad lagSendtSoknad() {
        return new SendtSoknad()
                .withBehandlingsId(BEHANDLINGSID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerFerdigDato(LocalDateTime.now());
    }
}