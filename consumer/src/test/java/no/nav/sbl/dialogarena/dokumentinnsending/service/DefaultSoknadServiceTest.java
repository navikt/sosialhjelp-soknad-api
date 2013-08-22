package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.content.ValueRetriever;
import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.core.context.SubjectHandlerUtils;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.VirusException;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandling;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventninger;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentInnhold;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.HOVEDSKJEMA;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBehandlingsstatus.UNDER_ARBEID;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_BEHANDLING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class DefaultSoknadServiceTest {

    @Mock
    private CmsContentRetriever cmsContentRetriever;
    @Mock
    private ValueRetriever valueRetriever;

    @InjectMocks
    private SoknadService soknadService = new DefaultSoknadService();

    @Mock
    private HenvendelsesBehandlingPortType henvendelseBehandlingWebService;

    @Mock
    private OppdatereHenvendelsesBehandlingPortType oppdatereService;
    @Mock
    private KodeverkClient kodeverkClient;

    private static final String HOVEDSKJEMA_DAGPENGER_ID = "NAV-200";
    private static final String NAV_VEDLEGG_UNDER_UTDANNING_ID = "NAV-300";
    private static final String NAV_VEDLEGG_SLUTTAARSAK_ID = "NAV-400";
    private static final String EKSTERNT_VEDLEGG_ARBEIDSAVTALE_ID = "500";
    private static final String EKSTERNT_VEDLEGG_LONNS_OG_TREKKOPPGAVE_ID = "600";
    private static final String EKSTERNT_VEDLEGG_PERMITTERINGSVARSEL_ID = "700";

    private KodeverkSkjema kodeverkSkjemaForDagpenger;
    private KodeverkSkjema kodeverkSkjemaForUnderUtdanning;
    private KodeverkSkjema kodeverkSkjemaForSluttAarsak;
    private KodeverkSkjema kodeverkSkjemaForArbeidsavtale;
    private KodeverkSkjema kodeverkSkjemaForInntektIfjorEllerSiste3Aar;
    private KodeverkSkjema kodeverkSkjemaPermitteringsvarsel;

    @Before
    public void setup() {
        cmsContentRetriever.setTeksterRetriever(valueRetriever);
        //Hovedskjema
        kodeverkSkjemaForDagpenger = createNAVSkjema(HOVEDSKJEMA_DAGPENGER_ID, "Krav om dagpenger");

        //NAV vedlegg
        kodeverkSkjemaForUnderUtdanning = createNAVSkjema(NAV_VEDLEGG_UNDER_UTDANNING_ID, "Bekreftelse fra lærestedet om at du er elev");
        kodeverkSkjemaForSluttAarsak = createNAVSkjema(NAV_VEDLEGG_SLUTTAARSAK_ID, "Bekreftelse på sluttårsak");

        //Eksterne vedlegg
        kodeverkSkjemaForArbeidsavtale = createEksterntVedlegg(EKSTERNT_VEDLEGG_ARBEIDSAVTALE_ID, "Kopi av arbeidsavtale");
        kodeverkSkjemaForInntektIfjorEllerSiste3Aar = createEksterntVedlegg(EKSTERNT_VEDLEGG_LONNS_OG_TREKKOPPGAVE_ID, "Lønns- og trekkoppgaver");
        kodeverkSkjemaPermitteringsvarsel = createEksterntVedlegg(EKSTERNT_VEDLEGG_PERMITTERINGSVARSEL_ID, "Permitteringsvarsel");

        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        System.setProperty(ModigSecurityConstants.SYSTEMUSER_USERNAME, "MD05");
        SubjectHandlerUtils.setEksternBruker("14125049975", 4, "doskjfløaskdjfø");
    }

    @Test
    public void skalHenteDokumentListeISortertRekkefolge() {

        String behandlingsId = "1";

        when(henvendelseBehandlingWebService.hentBrukerBehandling(behandlingsId)).thenReturn(createBehandling());

        when(kodeverkClient.hentKodeverkSkjemaForSkjemanummer(HOVEDSKJEMA_DAGPENGER_ID)).thenReturn(kodeverkSkjemaForDagpenger);
        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(NAV_VEDLEGG_SLUTTAARSAK_ID)).thenReturn(kodeverkSkjemaForSluttAarsak);
        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(NAV_VEDLEGG_UNDER_UTDANNING_ID)).thenReturn(kodeverkSkjemaForUnderUtdanning);
        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(EKSTERNT_VEDLEGG_ARBEIDSAVTALE_ID)).thenReturn(kodeverkSkjemaForArbeidsavtale);
        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(EKSTERNT_VEDLEGG_LONNS_OG_TREKKOPPGAVE_ID)).thenReturn(kodeverkSkjemaForInntektIfjorEllerSiste3Aar);
        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(EKSTERNT_VEDLEGG_PERMITTERINGSVARSEL_ID)).thenReturn(kodeverkSkjemaPermitteringsvarsel);

        DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);

        verify(henvendelseBehandlingWebService, times(1)).hentBrukerBehandling(behandlingsId);

        List<Dokument> dokumentListe = soknad.getDokumenter();
        assertThat(dokumentListe.size(), is(6));
        assertHovedSkjemaOgVedleggKommerIRiktigRekkefolge(dokumentListe);
        assertAlfabetiskRekkefolgeInnenforHverKodeverkskjemaType(dokumentListe);
    }

    @Test
    public void slettSoknadKasterException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).avbrytHenvendelse(anyString());

        try {
            soknadService.slettSoknad("1");
        } catch (ApplicationException ae) {
            Assert.assertThat(ae.getId(), is("ws.feil.slettsoknad"));
        }
    }

    @Test
    public void oppdaterBeskrivelseAnnetVedleggSkalKasteException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).oppdaterDokumentForventningBeskrivelse(anyLong(), anyString());

        Dokument dokument = new Dokument(Type.NAV_VEDLEGG);
        dokument.setDokumentForventningsId(1L);
        try {
            soknadService.oppdaterBeskrivelseAnnetVedlegg(dokument, "");
        } catch (ApplicationException ae) {
            assertThat(ae.getId(), is("ws.feil.hentdokumentforventning"));
        }
    }

    @Test
    public void hentSoknadKasterException() {
        when(henvendelseBehandlingWebService.hentBrukerBehandling(anyString())).thenThrow(SOAPFaultException.class);
        try {
            soknadService.hentSoknad("1");
        } catch (ApplicationException ae) {
            assertThat(ae.getId(), is("ws.feil.hentdokumentforventning"));
        }
    }

    @Test
    public void sjekkInternasjonaliseringsNokkelNaarOppdaterDokumentKasterException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).oppdaterDokumentForventning(1L, WSInnsendingsValg.SEND_I_POST);
        Dokument dokument = new Dokument(HOVEDSKJEMA);
        dokument.setDokumentForventningsId(1L);
        dokument.setInnsendingsvalg(InnsendingsValg.SEND_I_POST);

        try {
            soknadService.oppdaterInnsendingsvalg(dokument);
        } catch (ApplicationException ae) {
            assertThat(ae.getId(), is("ws.feil.oppdaterdokumentforventning"));
        }
    }

    @Test
    public void oppdaterDokumentForventningBeskrivelseKasterException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).oppdaterDokumentForventningBeskrivelse(1L, "Ny beskrivelse");
        Dokument dokument = new Dokument(HOVEDSKJEMA);
        dokument.setDokumentForventningsId(1L);
        dokument.setNavn("Ny beskrivelse");

        try {
            soknadService.oppdaterInnsendingsvalg(dokument);
        } catch (ApplicationException ae) {
            assertThat(ae.getId(), is("ws.feil.oppdaterdokumentforventning"));
        }
    }

    @Test
    public void hentDokumentInnholdSkalReturnereInputStreamFraWsDokument() throws IOException {
        WSDokument wsDokument = new WSDokument();
        DataHandler mock = mock(DataHandler.class);
        ByteArrayInputStream expected = new ByteArrayInputStream(new byte[0]);
        when(mock.getInputStream()).thenReturn(expected);
        wsDokument.setInnhold(mock);

        Dokument dokument = new Dokument(HOVEDSKJEMA);
        dokument.setDokumentId(10L);
        when(henvendelseBehandlingWebService.hentDokument(eq(dokument.getDokumentId()))).thenReturn(wsDokument);
        DokumentInnhold is = soknadService.hentDokumentInnhold(dokument);
        assertThat(is.hentInnholdSomBytes(), is(new byte[0]));
    }

    @Test(expected = VirusException.class)
    public void oppdaterInnholdSkalKasteVirusExceptionDersomVirusBlirFunnet() {
        DokumentInnhold innhold = new DokumentInnhold();
        innhold.setNavn("Navn");
        innhold.setOpplastetDato(DateTime.now());
        innhold.setInnhold(new byte[0]);

        Dokument dokument = new Dokument(HOVEDSKJEMA);
        dokument.setDokumentForventningsId(1L);

        SOAPFault faultMock = mock(SOAPFault.class);
        when(faultMock.getFaultString()).thenReturn("virus found");
        SOAPFaultException exceptionMock = mock(SOAPFaultException.class);
        when(exceptionMock.getFault()).thenReturn(faultMock);

        when(oppdatereService.opprettDokument(Matchers.any(WSDokumentInnhold.class), anyLong())).thenThrow(exceptionMock);

        soknadService.oppdaterInnhold(dokument, innhold);
    }

    private void assertHovedSkjemaOgVedleggKommerIRiktigRekkefolge(List<Dokument> dokumentListe) {
        assertTrue(dokumentListe.get(0).er(HOVEDSKJEMA));
        assertTrue(dokumentListe.get(1).er(Type.NAV_VEDLEGG));
        assertTrue(dokumentListe.get(2).er(Type.NAV_VEDLEGG));
        assertTrue(dokumentListe.get(3).er(Type.EKSTERNT_VEDLEGG));
        assertTrue(dokumentListe.get(4).er(Type.EKSTERNT_VEDLEGG));
        assertTrue(dokumentListe.get(5).er(Type.EKSTERNT_VEDLEGG));
    }

    private void assertAlfabetiskRekkefolgeInnenforHverKodeverkskjemaType(List<Dokument> dokumentListe) {
        assertThat(dokumentListe.get(0).getNavn(), is("Krav om dagpenger"));
        assertThat(dokumentListe.get(1).getNavn(), is("Bekreftelse fra lærestedet om at du er elev"));
        assertThat(dokumentListe.get(2).getNavn(), is("Bekreftelse på sluttårsak"));
        assertThat(dokumentListe.get(3).getNavn(), is("Kopi av arbeidsavtale"));
        assertThat(dokumentListe.get(4).getNavn(), is("Lønns- og trekkoppgaver"));
        assertThat(dokumentListe.get(5).getNavn(), is("Permitteringsvarsel"));
    }

    private WSBrukerBehandling createBehandling() {
        List<WSDokumentForventning> dokumentForventninger = Arrays.asList(
                createDokumentForventning(HOVEDSKJEMA_DAGPENGER_ID, WSInnsendingsValg.LASTET_OPP, true),
                createDokumentForventning(NAV_VEDLEGG_SLUTTAARSAK_ID, WSInnsendingsValg.LASTET_OPP, false),
                createDokumentForventning(NAV_VEDLEGG_UNDER_UTDANNING_ID, WSInnsendingsValg.SEND_I_POST, false),
                createDokumentForventning(EKSTERNT_VEDLEGG_ARBEIDSAVTALE_ID, WSInnsendingsValg.SEND_I_POST, false),
                createDokumentForventning(EKSTERNT_VEDLEGG_LONNS_OG_TREKKOPPGAVE_ID, WSInnsendingsValg.SEND_I_POST, false),
                createDokumentForventning(EKSTERNT_VEDLEGG_PERMITTERINGSVARSEL_ID, WSInnsendingsValg.SEND_I_POST, false));

        return new WSBrukerBehandling()
                .withBrukerBehandlingType(DOKUMENT_BEHANDLING)
                .withDokumentForventninger(new WSDokumentForventninger().withDokumentForventning(dokumentForventninger))
                .withStatus(UNDER_ARBEID);
    }

    private WSDokumentForventning createDokumentForventning(String kodeverkId, WSInnsendingsValg innsendingsValg,
                                                            boolean hovedskjema) {
        WSDokumentForventning dokumentForventning = new WSDokumentForventning();
        dokumentForventning.setKodeverkId(kodeverkId);
        dokumentForventning.setInnsendingsValg(innsendingsValg);
        dokumentForventning.setHovedskjema(hovedskjema);
        return dokumentForventning;
    }

    private KodeverkSkjema createEksterntVedlegg(String kodeverkId, String navn) {
        return createKodeverkSkjema(kodeverkId, navn);
    }

    private KodeverkSkjema createNAVSkjema(String kodeverkId, String navn) {
        KodeverkSkjema skjema = createKodeverkSkjema(kodeverkId, navn);
        skjema.setUrl("link");
        skjema.setSkjemanummer("NAV");
        return skjema;
    }

    private KodeverkSkjema createKodeverkSkjema(String kodeverkId, String navn) {
        KodeverkSkjema kodeverkSkjema = new KodeverkSkjema();
        kodeverkSkjema.setSkjemanummer(kodeverkId);
        kodeverkSkjema.setTittel(navn);
        kodeverkSkjema.setBeskrivelse("");
        kodeverkSkjema.setVedleggsid("A1");
        return kodeverkSkjema;
    }
}