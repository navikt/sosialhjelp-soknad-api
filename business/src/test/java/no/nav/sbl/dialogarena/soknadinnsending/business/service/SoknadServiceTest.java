package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.activation.DataHandler;
import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.RUTES_I_BRUT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private Kodeverk kodeverk;
    @Mock
    private NavMessageSource navMessageSource;

    @Mock
    private StartDatoService startDatoService;

    @Mock
    private FaktaService faktaService;

    @InjectMocks
    private SoknadService soknadService;

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadRepository.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
    }

    @Test
    public void skalPopulereFraHenvendelseNaarSoknadIkkeFinnes() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg");
        Vedlegg vedleggCheck = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medData(new byte[]{1, 2, 3});
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medId(11L).medVedlegg(Arrays.asList(vedlegg)).medStatus(UNDER_ARBEID);
        WebSoknad soknadCheck = new WebSoknad().medBehandlingId("123").medId(11L).medVedlegg(Arrays.asList(vedleggCheck));

        when(henvendelsesConnector.hentSoknad("123")).thenReturn(
                new WSHentSoknadResponse()
                        .withBehandlingsId("123")
                        .withStatus(WSStatus.UNDER_ARBEID.toString())
                        .withAny(new XMLMetadataListe()
                                .withMetadata(
                                        new XMLHovedskjema().withUuid("uidHovedskjema"),
                                        new XMLVedlegg().withUuid("uidVedlegg")))
        );
        when(soknadRepository.hentMedBehandlingsId("123")).thenReturn(null, soknad, soknad);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(soknad, baos);
        DataHandler handler = mock(DataHandler.class);
        when(fillagerService.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        when(fillagerService.hentFiler("123"))
                .thenReturn(Arrays.asList(
                        new WSInnhold().withUuid("uidVedlegg").withInnhold(handler)
                ));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                OutputStream os = (OutputStream) invocation.getArguments()[0];
                os.write(new byte[]{1, 2, 3});
                return null;
            }
        }).when(handler).writeTo(any(OutputStream.class));
        WebSoknad webSoknad = soknadService.hentSoknadMedBehandlingsId("123");
        soknadService.hentSoknadMedBehandlingsId("123");
        verify(soknadRepository, atMost(1)).populerFraStruktur(eq(soknadCheck));
        verify(vedleggRepository).lagreVedleggMedData(11L, 4L, vedleggCheck);
        assertThat(webSoknad.getSoknadId(), is(equalTo(11L)));
    }

    @Test
    public void skalSendeSoknad() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(2L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("L7")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(
                new WebSoknad().medAktorId("123456")
                        .medBehandlingId("123")
                        .medUuid("uidHovedskjema")
                        .medskjemaNummer(DAGPENGER)
                        .medFaktum(new Faktum().medKey("personalia"))
                        .medVedlegg(vedlegg));

        when(vedleggRepository.hentPaakrevdeVedlegg(1L)).thenReturn(vedlegg);

        when(vedleggRepository.hentVedleggForskjemaNummer(1L, null, Kodeverk.KVITTERING))
                .thenReturn(new Vedlegg()
                                .medFillagerReferanse("kvitteringRef")
                                .medSkjemaNummer(Kodeverk.KVITTERING)
                                .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                                .medStorrelse(3L)
                                .medAntallSider(1)
                );

        soknadService.sendSoknad(1L, new byte[]{1, 2, 3});
        verify(henvendelsesConnector).avsluttSoknad(eq("123"), refEq(new XMLHovedskjema()
                        .withUuid("uidHovedskjema")
                        .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.toString())
                        .withJournalforendeEnhet(RUTES_I_BRUT)
                        .withFilnavn(DAGPENGER)
                        .withFilstorrelse("3")
                        .withMimetype("application/pdf")
                        .withSkjemanummer(DAGPENGER)),
                refEq(
                        new XMLVedlegg()
                                .withUuid("uidVedlegg1")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.toString())
                                .withFilnavn("Test Annet vedlegg")
                                .withTilleggsinfo("Test Annet vedlegg")
                                .withFilstorrelse("2")
                                .withSideantall(3)
                                .withMimetype("application/pdf")
                                .withSkjemanummer("N6")),
                refEq(
                        new XMLVedlegg()
                                .withInnsendingsvalg(XMLInnsendingsvalg.SENDES_IKKE.toString())
                                .withTilleggsinfo("")
                                .withSkjemanummer("L7")
                                .withFilnavn("L7")),
                refEq(
                        new XMLVedlegg()
                                .withUuid("kvitteringRef")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.toString())
                                .withFilnavn(Kodeverk.KVITTERING)
                                .withTilleggsinfo("")
                                .withFilstorrelse("3")
                                .withSideantall(1)
                                .withMimetype("application/pdf")
                                .withSkjemanummer(Kodeverk.KVITTERING))
        );
    }

    @Test(expected = ApplicationException.class)
    public void skalIkkeSendeSoknadMedN6VedleggSomIkkeErSendtInn() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("L7")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(
                new WebSoknad().medAktorId("123456")
                        .medBehandlingId("123")
                        .medUuid("uidHovedskjema")
                        .medskjemaNummer(DAGPENGER)
                        .medFaktum(new Faktum().medKey("personalia"))
                        .medVedlegg(vedlegg));

        when(vedleggRepository.hentPaakrevdeVedlegg(1L)).thenReturn(vedlegg);

        soknadService.sendSoknad(1L, new byte[]{1, 2, 3});
    }

    @Test
    public void skalSetteDelsteg() {
        soknadService.settDelsteg(1L, OPPRETTET);
        verify(soknadRepository).settDelstegstatus(1L, OPPRETTET);
    }

    @Test
    public void skalHenteSoknad() {
        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(new WebSoknad().medId(1L));
        when(vedleggRepository.hentPaakrevdeVedlegg(1L)).thenReturn(new ArrayList<Vedlegg>());
        assertThat(soknadService.hentSoknad(1L), is(equalTo(new WebSoknad().medId(1L).medVedlegg(new ArrayList<Vedlegg>()))));
    }

    @Test
    public void skalHenteSoknadEier() {
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medAktorId("123"));
        assertThat(soknadService.hentSoknadEier(1L), is(equalTo("123")));
    }






    @Test
    public void skalStarteSoknad() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString())).thenReturn("123");
        when(soknadRepository.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(soknadRepository.hentFaktum(anyLong(), anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        soknadService.startSoknad(DAGPENGER);

        ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
        String bruker = StaticSubjectHandler.getSubjectHandler().getUid();
        verify(henvendelsesConnector).startSoknad(eq(bruker), eq(DAGPENGER), uid.capture());
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medUuid(uid.getValue())
                .medskjemaNummer(DAGPENGER)
                .medAktorId(bruker)
                .medOppretteDato(new DateTime())
                .medStatus(UNDER_ARBEID)
                .medDelstegStatus(OPPRETTET);
        verify(soknadRepository).opprettSoknad(soknad);
        verify(soknadRepository, atLeastOnce()).lagreFaktum(anyLong(), any(Faktum.class));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void skalLagreFaktumForLonnsOgTrekkoppgaveMedValueFalseDersomSoknadStartesIJanuarEllerFebruar() {
        Long soknadId = 0L;
        Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue("false");

        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString())).thenReturn("123");
        when(soknadRepository.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(soknadRepository.hentFaktum(anyLong(), anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(startDatoService.erJanuarEllerFebruar()).thenReturn(false);
        when(soknadRepository.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        soknadService.startSoknad(DAGPENGER);

        verify(faktaService, times(1)).lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum, "");
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void skalLagreFaktumForLonnsOgTrekkoppgaveMedValueTrueDersomSoknadStartesIJanuarEllerFebruar() {
        Long soknadId = 0L;
        Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue("true");

        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString())).thenReturn("123");
        when(soknadRepository.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(soknadRepository.hentFaktum(anyLong(), anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(startDatoService.erJanuarEllerFebruar()).thenReturn(true);
        when(soknadRepository.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        soknadService.startSoknad(DAGPENGER);

        verify(faktaService, times(1)).lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum, "");
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void skalStarteForsteEttersending() {
        String behandlingsId = "soknadBehandlingId";
        String ettersendingsBehandlingId = "ettersendingBehandlingId";

        DateTime innsendingsDato = DateTime.now();

        WSBehandlingskjedeElement behandlingsKjedeElement = new WSBehandlingskjedeElement()
                .withBehandlingsId(behandlingsId)
                .withInnsendtDato(innsendingsDato)
                .withStatus(WSStatus.FERDIG.toString());

        WSHentSoknadResponse orginalInnsending = new WSHentSoknadResponse()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.FERDIG.toString())
                .withInnsendtDato(innsendingsDato)
                .withAny(new XMLMetadataListe()
                        .withMetadata(
                                new XMLHovedskjema().withUuid("uidHovedskjema"),
                                new XMLVedlegg().withSkjemanummer("MittSkjemaNummer")));

        WSHentSoknadResponse ettersendingResponse = new WSHentSoknadResponse()
                .withBehandlingsId(ettersendingsBehandlingId)
                .withStatus(WSStatus.UNDER_ARBEID.toString())
                .withAny(new XMLMetadataListe()
                        .withMetadata(
                                new XMLHovedskjema().withUuid("uidHovedskjema"),
                                new XMLVedlegg().withSkjemanummer("MittSkjemaNummer").withInnsendingsvalg(Vedlegg.Status.SendesSenere.name())));

        when(henvendelsesConnector.hentSoknad(ettersendingsBehandlingId)).thenReturn(ettersendingResponse);
        when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Arrays.asList(behandlingsKjedeElement));
        when(henvendelsesConnector.startEttersending(orginalInnsending)).thenReturn(ettersendingsBehandlingId);

        Long soknadId = 11L;
        Faktum soknadInnsendingsDatoFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendingsDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
        when(soknadRepository.hentFaktum(anyLong(), anyLong())).thenReturn(soknadInnsendingsDatoFaktum);

        Long ettersendingSoknadId = soknadService.startEttersending(behandlingsId);
        verify(faktaService).lagreSystemFaktum(anyLong(), any(Faktum.class), anyString());
        assertNotNull(ettersendingSoknadId);
    }

    @Test(expected = ApplicationException.class)
    public void skalIkkeKunneStarteEttersendingPaaUferdigSoknad() {
        String behandlingsId = "UferdigSoknadBehandlingId";

        WSBehandlingskjedeElement behandlingskjedeElement = new WSBehandlingskjedeElement()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.UNDER_ARBEID.toString());

        WSHentSoknadResponse orginalInnsending = new WSHentSoknadResponse()
                .withBehandlingsId(behandlingsId)
                .withStatus(WSStatus.UNDER_ARBEID.toString());
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(Arrays.asList(behandlingskjedeElement));
        when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);

        soknadService.startEttersending(behandlingsId);
    }

    @Test
    public void skalAvbryteSoknad() {
        when(soknadRepository.hentSoknad(11L)).thenReturn(new WebSoknad().medBehandlingId("123"));
        soknadService.avbrytSoknad(11L);
        verify(soknadRepository).slettSoknad(11L);
        verify(henvendelsesConnector).avbrytSoknad("123");
    }

    @Test
    public void skalHenteSoknadsIdForEttersendingTilBehandlingskjedeId() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.optional(soknad));

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad.getSoknadId(), is(1L));
    }

    @Test
    public void skalFaNullNarManProverAHenteEttersendingMedBehandlingskjedeIdSomIkkeHarNoenEttersending() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.<WebSoknad>none());

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad, is(nullValue()));
    }
}
