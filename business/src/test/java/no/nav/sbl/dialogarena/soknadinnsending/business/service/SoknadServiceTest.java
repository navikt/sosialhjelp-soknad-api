package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.detect.Detect.IS_PDF;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.SKJEMA_VALIDERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.RUTES_I_BRUT;
import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private HenvendelseConnector henvendelsesConnector;
    @Mock
    private FillagerConnector fillagerConnector;
    @Mock
    private Kodeverk kodeverk;
    @Mock
    private NavMessageSource navMessageSource;

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
    public void skalAKonvertereFilerVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(11L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/images/bilde.jpg"));
        List<Long> ids = soknadService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(11L));
    }

    @Test
    public void skalHenteVedlegg() {
        soknadService.hentVedlegg(11L, 1L, false);
        verify(vedleggRepository).hentVedlegg(11L, 1L);
        soknadService.hentVedlegg(11L, 1L, true);
        verify(vedleggRepository).hentVedleggMedInnhold(11L, 1L);
    }

    @Test
    public void skalKonverterePdfVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(10L, 11L, 12L, 13L, 14L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/pdfs/navskjema.pdf"));
        List<Long> ids = soknadService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(10L, 11L, 12L, 13L, 14L));
    }

    @Test
    public void skalGenerereVedleggFaktum() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medAntallSider(1).medVedleggId(2L).medFillagerReferanse(vedlegg.getFillagerReferanse()).medData(bytes);
        when(vedleggRepository.hentVedlegg(1L, 2L)).thenReturn(vedlegg);
        when(vedleggRepository.hentVedleggUnderBehandling(1L, null, "L6")).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(10L)));
        when(vedleggRepository.hentVedleggData(1L, 10L)).thenReturn(bytes);
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medBehandlingId("123").medAktorId("234"));
        soknadService.genererVedleggFaktum(1L, 2L);
        vedleggSjekk.setData(vedlegg.getData());
        vedleggSjekk.medStorrelse((long) vedlegg.getData().length);
        verify(vedleggRepository).lagreVedleggMedData(1L, 2L, vedleggSjekk);
        verify(fillagerConnector).lagreFil(eq("123"), eq(vedleggSjekk.getFillagerReferanse()), eq("234"), any(InputStream.class));
    }

    @Test
    public void skalGenererForhandsvisning() throws IOException {
        when(vedleggRepository.hentVedleggData(11L, 1L)).thenReturn(getBytesFromFile("/pdfs/minimal.pdf"));
        byte[] bytes = soknadService.lagForhandsvisning(11L, 1L, 0);
        assertThat(bytes, instanceOf(byte[].class));
    }

    @Test
    public void skalSletteVedlegg() {
        soknadService.slettVedlegg(1L, 2L);
        verify(vedleggRepository).slettVedlegg(1L, 2L);
        verify(soknadRepository).settDelstegstatus(1L, SKJEMA_VALIDERT);
    }

    @Test
    public void skalHentePaakrevdeVedlegg() {
        Map<Kodeverk.Nokkel, String> map = new HashMap<>();
        map.put(Kodeverk.Nokkel.TITTEL, "tittel");
        map.put(Kodeverk.Nokkel.URL, "url");
        when(kodeverk.getKoder("L6")).thenReturn(map);
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medTittel("tittel").medUrl("URL", "url")
                .medFillagerReferanse(vedlegg.getFillagerReferanse());
        when(vedleggRepository.hentPaakrevdeVedlegg(1L)).thenReturn(Arrays.asList(vedlegg));
        List<Vedlegg> vedleggs = soknadService.hentPaakrevdeVedlegg(1L);
        assertThat(vedleggs.get(0), is(equalTo(vedleggSjekk)));
    }

    @Test
    public void skalPopulereFraHenvendelseNaarSoknadIkkeFinnes() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg");
        Vedlegg vedleggCheck = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medData(new byte[]{1, 2, 3});
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medId(11L).medVedlegg(Arrays.asList(vedlegg));
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
        when(fillagerConnector.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        when(fillagerConnector.hentFiler("123"))
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
        Long id = soknadService.hentSoknadMedBehandlinsId("123");
        soknadService.hentSoknadMedBehandlinsId("123");
        verify(soknadRepository, atMost(1)).populerFraStruktur(eq(soknadCheck));
        verify(vedleggRepository).lagreVedleggMedData(11L, 4L, vedleggCheck);
        assertThat(id, is(equalTo(11L)));
    }

    @Test
    public void skalSendeSoknad() {
        List<Vedlegg> paakrevdeVedlegg = new ArrayList<>();
        paakrevdeVedlegg.add(new Vedlegg()
                .medSkjemaNummer("N6")
                .medFillagerReferanse("uidVedlegg1")
                .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                .medStorrelse(2L)
                .medNavn("Test Annet vedlegg")
                .medAntallSider(3));
        paakrevdeVedlegg.add( new Vedlegg()
                .medSkjemaNummer("L7")
                .medInnsendingsvalg(Vedlegg.Status.SendesIkke));
        when(soknadService.hentPaakrevdeVedlegg(1L)).thenReturn(paakrevdeVedlegg);
        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(
                new WebSoknad().medAktorId("123456")
                        .medBehandlingId("123")
                        .medUuid("uidHovedskjema")
                        .medskjemaNummer(DAGPENGER)
                        .medFaktum(new Faktum().medKey("personalia"))
                        .medVedlegg(Arrays.asList(
                                new Vedlegg()
                                        .medSkjemaNummer("N6")
                                        .medFillagerReferanse("uidVedlegg1")
                                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                                        .medStorrelse(2L)
                                        .medNavn("Test Annet vedlegg")
                                        .medAntallSider(3),
                                new Vedlegg()
                                        .medSkjemaNummer("L7")
                                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke)))
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
                                .withFilnavn("L7")));
    }


    @Test
    public void skalSendeEttersending() {
        String opprinneligBehandlingsId = "100000000TEST";
        String ettersendingsBehandlingId = "1000ETTERSENDING";

        WSHentSoknadResponse wsHentSoknadResponse = new WSHentSoknadResponse()
                .withBehandlingsId(opprinneligBehandlingsId)
                .withStatus(WSStatus.UNDER_ARBEID.toString())
                .withAny(new XMLMetadataListe()
                        .withMetadata(
                                new XMLHovedskjema().withUuid("uidHovedskjema"),
                                new XMLVedlegg().withUuid("uidVedlegg")));

        when(henvendelsesConnector.hentSisteBehandlingIBehandlingskjede(opprinneligBehandlingsId)).thenReturn(
                wsHentSoknadResponse
        );

        when(henvendelsesConnector.startEttersending(wsHentSoknadResponse)).thenReturn(ettersendingsBehandlingId);

        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(
                new WebSoknad().medAktorId("123456")
                        .medBehandlingId(ettersendingsBehandlingId)
                        .medUuid("uidHovedskjema")
                        .medskjemaNummer(DAGPENGER)
                        .medFaktum(new Faktum().medKey("personalia"))
                        .medVedlegg(Arrays.asList(
                                new Vedlegg()
                                        .medSkjemaNummer("N6")
                                        .medFillagerReferanse("uidVedlegg1")
                                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                                        .medStorrelse(2L)
                                        .medNavn("Test Annet vedlegg")
                                        .medAntallSider(3),
                                new Vedlegg()
                                        .medSkjemaNummer("L7")
                                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke)))
        );

        soknadService.sendEttersending(1L, opprinneligBehandlingsId);
        verify(henvendelsesConnector).avsluttSoknad(eq(ettersendingsBehandlingId), refEq(new XMLHovedskjema()
                .withUuid("uidHovedskjema")),
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
                                .withFilnavn("L7")));
    }

    @Test
    public void skalSetteDelsteg() {
        soknadService.settDelsteg(1L, OPPRETTET);
        verify(soknadRepository).settDelstegstatus(1L, OPPRETTET);
    }

    @Test
    public void skalHenteSoknad() {
        when(soknadRepository.hentSoknadMedData(1L)).thenReturn(new WebSoknad().medId(1L));
        assertThat(soknadService.hentSoknad(1L), is(equalTo(new WebSoknad().medId(1L))));
    }

    @Test
    public void skalHenteSoknadEier() {
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medAktorId("123"));
        assertThat(soknadService.hentSoknadEier(1L), is(equalTo("123")));
    }

    @Test
    public void skalLagreSoknadFelt() {
        Faktum faktum = new Faktum().medKey("ikkeavtjentverneplikt").medValue("false").medFaktumId(1L);
        when(soknadRepository.lagreFaktum(1L, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("T3").medSoknadId(1L).medInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
        when(vedleggRepository.hentVedleggForskjemaNummer(1L, null, "T3")).thenReturn(vedlegg);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), any(byte[].class))).thenReturn(4L);
        soknadService.lagreSoknadsFelt(1L, faktum);
        verify(soknadRepository).settSistLagretTidspunkt(1L);
        when(soknadRepository.hentBarneFakta(1L, faktum.getFaktumId())).thenReturn(Arrays.asList(new Faktum().medKey("subkey")));

        //Verifiser vedlegg sjekker.
        verify(soknadRepository).lagreFaktum(1L, faktum);
        verify(vedleggRepository).lagreVedlegg(1L, 4L, vedlegg.medInnsendingsvalg(Vedlegg.Status.VedleggKreves));

    }

    @Test
    public void skalIkkeoppdatereDelstegstatusVedEpost() {
        Faktum faktum = new Faktum().medKey("epost").medValue("false").medFaktumId(1L);
        when(soknadRepository.lagreFaktum(1L, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        soknadService.lagreSoknadsFelt(1L, faktum);
        verify(soknadRepository, never()).settDelstegstatus(anyLong(), any(DelstegStatus.class));
    }

    @Test
    public void skalLagreVedlegg() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L);
        soknadService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedNegradertInnsendingsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.SendesIkke);
        soknadService.lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalKunneLagreVedleggMedSammeInnsendinsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.LastetOpp);
        soknadService.lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalKunneLagreVedleggMedOppgradertInnsendingsStatus() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.SendesIkke);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.SendesSenere);
        soknadService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedPrioritetMindreEllerLik1() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.VedleggKreves);
        soknadService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test
    public void skalSletteBrukerfaktum() {
        when(vedleggRepository.hentVedleggForFaktum(1L, 1L)).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(111L).medSkjemaNummer("a1").medFaktumId(111L)));
        when(soknadRepository.hentFaktum(1L, 1L)).thenReturn(new Faktum().medKey("key"));
        soknadService.slettBrukerFaktum(1L, 1L);
        verify(vedleggRepository).slettVedleggOgData(1L, 111L, "a1");
        verify(soknadRepository).slettBrukerFaktum(1L, 1L);
        verify(soknadRepository).settDelstegstatus(1L, DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalLagreSystemfaktumUtenUnuque() {
        Faktum faktum = new Faktum().medKey("personalia").medValue("tester").medSoknadId(1L);
        when(soknadRepository.lagreFaktum(anyLong(), any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        soknadService.lagreSystemFaktum(1L, faktum, "");
        verify(soknadRepository).lagreFaktum(1L, faktum, true);
    }

    @Test
    public void skalLagreSystemfaktummedUniqueSomFinnes() {
        Faktum faktum = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L);
        Faktum faktumSjekk = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L).medType(Faktum.FaktumType.SYSTEMREGISTRERT);

        when(soknadRepository.lagreFaktum(anyLong(), any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        when(soknadRepository.hentSystemFaktumList(1L, faktum.getKey())).thenReturn(Arrays.asList(
                new Faktum().medFaktumId(5L).medKey("personalia").medSystemProperty("fno", "123"),
                new Faktum().medFaktumId(6L).medKey("personalia").medSystemProperty("fno", "124")));
        soknadService.lagreSystemFaktum(1L, faktum, "fno");
        verify(soknadRepository).lagreFaktum(1L, faktumSjekk.medFaktumId(5L), true);
    }

    @Test
    public void skalStarteSoknad() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString())).thenReturn("123");
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
        verify(soknadRepository).lagreFaktum(anyLong(), any(Faktum.class));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void skalAvbryteSoknad() {
        when(soknadRepository.hentSoknad(11L)).thenReturn(new WebSoknad().medBehandlingId("123"));
        soknadService.avbrytSoknad(11L);
        verify(soknadRepository).avbryt(11L);
        verify(henvendelsesConnector).avbrytSoknad("123");
    }


}
