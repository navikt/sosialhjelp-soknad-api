package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import javax.activation.DataHandler;
import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.RUTES_I_BRUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadDataFletterTest {

    public static final String SKJEMA_NUMMER = "NAV 04-01.03";
    private static final List<String> SKJEMANUMMER_TILLEGGSSTONAD = asList("NAV 11-12.12", "NAV 11-12.13");
    private static final Vedlegg KVITTERING_REF = new Vedlegg()
            .medFillagerReferanse("kvitteringRef")
            .medSkjemaNummer(Kodeverk.KVITTERING)
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medStorrelse(3L)
            .medAntallSider(1);

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private VedleggService vedleggService;
    @Mock
    private FaktaService faktaService;
    @Mock
    private WebSoknadConfig config;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Mock
    private StartDatoUtil startDatoUtil;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private PersonaliaBolk personaliaBolk;
    @Mock
    private BarnBolk barnBolk;
    @Mock
    private ArbeidsforholdBolk arbeidsforholdBolk;
    @Mock
    ApplicationContext applicationContex;
    @Mock
    SoknadMetricsService soknadMetricsService;

    @Captor
    ArgumentCaptor<XMLHovedskjema> argument;

    @InjectMocks
    private SoknadDataFletter soknadServiceUtil;

    @InjectMocks
    private AlternativRepresentasjonService alternativRepresentasjonService;
    @InjectMocks
    private EkstraMetadataService ekstraMetadataService;


    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        Map<String, BolkService> bolker = new HashMap<>();
        bolker.put(PersonaliaBolk.class.getName(), personaliaBolk);
        bolker.put(BarnBolk.class.getName(), barnBolk);
        bolker.put(ArbeidsforholdBolk.class.getName(), arbeidsforholdBolk);
        when(applicationContex.getBeansOfType(BolkService.class)).thenReturn(bolker);

        soknadServiceUtil.initBolker();
        soknadServiceUtil.alternativRepresentasjonService = alternativRepresentasjonService;
        soknadServiceUtil.ekstraMetadataService = ekstraMetadataService;
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(lokalDb.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
        when(config.getSoknadBolker(any(WebSoknad.class), any(List.class))).thenReturn(new ArrayList());
        when(config.hentStruktur(any(String.class))).thenReturn(new SoknadStruktur());
        when(kravdialogInformasjonHolder.hentAlleSkjemanumre()).thenReturn(new KravdialogInformasjonHolder().hentAlleSkjemanumre());
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(new KravdialogInformasjonHolder().getSoknadsKonfigurasjoner().get(0));
    }

    @Test
    public void skalStarteSoknad() {
        final long soknadId = 69L;
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString(), Matchers.any(SoknadType.class))).thenReturn("123");
        when(lokalDb.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.hentFaktum(anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        when(lokalDb.hentSoknadMedData(soknadId)).thenReturn(new WebSoknad().medId(soknadId));
        soknadServiceUtil.startSoknad(DAGPENGER);

        ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
        String bruker = StaticSubjectHandler.getSubjectHandler().getUid();
        verify(henvendelsesConnector).startSoknad(eq(bruker), eq(DAGPENGER), uid.capture(), Matchers.any(SoknadType.class));
        WebSoknad soknad = new WebSoknad()
                .medId(soknadId)
                .medBehandlingId("123")
                .medUuid(uid.getValue())
                .medskjemaNummer(DAGPENGER)
                .medAktorId(bruker)
                .medOppretteDato(new DateTime())
                .medStatus(UNDER_ARBEID)
                .medDelstegStatus(OPPRETTET);
        verify(lokalDb).opprettSoknad(soknad);
        verify(faktaService, atLeastOnce()).lagreFaktum(anyLong(), any(Faktum.class));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test(expected = ApplicationException.class)
    public void skalIkkeSendeSoknadMedN6VedleggSomIkkeErSendtInn() {
        String behandlingsId = "10000000ABC";
        List<Vedlegg> vedlegg = asList(
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

        WebSoknad soknad = new WebSoknad().medAktorId("123456")
                .medBehandlingId(behandlingsId)
                .medUuid("uidHovedskjema")
                .medskjemaNummer(DAGPENGER)
                .medFaktum(new Faktum().medKey("personalia"))
                .medVedlegg(vedlegg)
                .medId(1L);
        when(lokalDb.hentSoknadMedVedlegg(behandlingsId)).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);

        soknadServiceUtil.sendSoknad(behandlingsId, new byte[]{1, 2, 3}, null);
    }

    @Test
    public void skalSendeSoknad() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(2L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("L8")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        String behandlingsId = "123";
        WebSoknad webSoknad = new WebSoknad().medId(1L)
                .medAktorId("123456")
                .medBehandlingId(behandlingsId)
                .medUuid("uidHovedskjema")
                .medskjemaNummer(DAGPENGER)
                .medFaktum(new Faktum().medKey("personalia"))
                .medVedlegg(vedlegg);
        when(lokalDb.hentSoknadMedVedlegg(behandlingsId)).thenReturn(
                webSoknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(webSoknad);

        when(vedleggRepository.hentVedleggForskjemaNummer(1L, null, Kodeverk.KVITTERING))
                .thenReturn(KVITTERING_REF);
        when(vedleggService.hentVedleggOgKvittering(webSoknad)).thenReturn(mockHentVedleggForventninger(webSoknad));

        when(kravdialogInformasjonHolder.hentKonfigurasjon(SKJEMA_NUMMER)).thenReturn(new KravdialogInformasjonHolder().hentKonfigurasjon("NAV 04-01.03"));
        soknadServiceUtil.sendSoknad(behandlingsId, new byte[]{1, 2, 3}, new byte[]{4,5,6});

        verify(henvendelsesConnector).avsluttSoknad(eq(behandlingsId), argument.capture(),
                refEq(new XMLVedlegg[] {
                        new XMLVedlegg()
                                .withUuid("uidVedlegg1")
                                .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.toString())
                                .withFilnavn("Test Annet vedlegg")
                                .withTilleggsinfo("Test Annet vedlegg")
                                .withFilstorrelse("2")
                                .withSideantall(3)
                                .withMimetype("application/pdf")
                                .withSkjemanummer("N6"),
                                new XMLVedlegg()
                                        .withInnsendingsvalg(XMLInnsendingsvalg.SENDES_IKKE.toString())
                                        .withTilleggsinfo("")
                                        .withSkjemanummer("L8")
                                        .withFilnavn("L8"),
                                new XMLVedlegg()
                                        .withUuid("kvitteringRef")
                                        .withInnsendingsvalg(XMLInnsendingsvalg.LASTET_OPP.toString())
                                        .withFilnavn(Kodeverk.KVITTERING)
                                        .withTilleggsinfo("")
                                        .withFilstorrelse("3")
                                        .withSideantall(1)
                                        .withMimetype("application/pdf")
                                        .withSkjemanummer(Kodeverk.KVITTERING)
                })
        , any());

        XMLHovedskjema xmlHovedskjema = argument.getValue();
        assertThat(xmlHovedskjema.getJournalforendeEnhet()).isEqualTo(RUTES_I_BRUT);
        assertThat(xmlHovedskjema.getUuid()).isEqualTo("uidHovedskjema");
        assertThat(xmlHovedskjema.getInnsendingsvalg()).isEqualTo(XMLInnsendingsvalg.LASTET_OPP.toString());
        assertThat(xmlHovedskjema.getFilnavn()).isEqualTo(DAGPENGER);
        assertThat(xmlHovedskjema.getFilstorrelse()).isEqualTo("3");
        assertThat(xmlHovedskjema.getMimetype()).isEqualTo("application/pdf");
        assertThat(xmlHovedskjema.getSkjemanummer()).isEqualTo(DAGPENGER);
        assertThat(xmlHovedskjema.getAlternativRepresentasjonListe().getAlternativRepresentasjon().get(0))
                .isEqualToComparingFieldByField(
                        new XMLAlternativRepresentasjon()
                        .withFilnavn(DAGPENGER)
                        .withFilstorrelse("3")
                        .withMimetype("application/pdf-fullversjon")
                        .withUuid(xmlHovedskjema.getAlternativRepresentasjonListe().getAlternativRepresentasjon().get(0).getUuid())
                );
        assertThat(xmlHovedskjema.getAlternativRepresentasjonListe().getAlternativRepresentasjon().get(0)).isNotEqualTo(xmlHovedskjema.getUuid());
    }

    @Test
    public void skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123")
                .medskjemaNummer(DAGPENGER)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medId(1L);
        when(lokalDb.hentSoknad("123")).thenReturn(
                soknad);
        when(config.getSoknadBolker(any(WebSoknad.class), anyListOf(BolkService.class))).thenReturn(asList(personaliaBolk, barnBolk));
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);
        soknadServiceUtil.hentSoknad("123", true, true);
        verify(personaliaBolk, times(1)).genererSystemFakta(anyString(), anyLong());
        verify(barnBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    public void skalPopulereFraHenvendelseNaarSoknadIkkeFinnes() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg");
        Vedlegg vedleggCheck = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg").medData(new byte[]{1, 2, 3});
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(asList(vedlegg)).medStatus(UNDER_ARBEID);
        WebSoknad soknadCheck = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(asList(vedleggCheck));

        when(henvendelsesConnector.hentSoknad("123")).thenReturn(
                new WSHentSoknadResponse()
                        .withBehandlingsId("123")
                        .withStatus(WSStatus.UNDER_ARBEID.toString())
                        .withAny(new XMLMetadataListe()
                                .withMetadata(
                                        new XMLHovedskjema().withUuid("uidHovedskjema"),
                                        new XMLVedlegg().withUuid("uidVedlegg")))
        );
        when(lokalDb.hentSoknad("123")).thenReturn(null, soknad, soknad);
        when(lokalDb.hentSoknadMedVedlegg("123")).thenReturn(soknad, soknad);
        when(lokalDb.hentSoknadMedData(11L)).thenReturn(soknad);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(soknad, baos);
        DataHandler handler = mock(DataHandler.class);
        when(fillagerService.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        when(fillagerService.hentFiler("123"))
                .thenReturn(asList(
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
        WebSoknad webSoknad = soknadServiceUtil.hentSoknad("123", true, false);
        soknadServiceUtil.hentSoknad("123", true, false);
        verify(lokalDb, atMost(1)).populerFraStruktur(eq(soknadCheck));
        assertThat(webSoknad.getSoknadId()).isEqualTo(11L);
    }

    @Test
    public void lagreSystemfakumSomDefinertForSoknadVedHenting() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(DAGPENGER)
                .medId(1L);
        when(lokalDb.hentSoknad("123")).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);

        when(config.getSoknadBolker(any(WebSoknad.class), anyListOf(BolkService.class))).thenReturn(asList(personaliaBolk, barnBolk));
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        soknadServiceUtil.hentSoknad("123", true, true);
        verify(personaliaBolk, times(1)).genererSystemFakta(anyString(), anyLong());
        verify(barnBolk, times(1)).genererSystemFakta(anyString(), anyLong());
        verify(arbeidsforholdBolk, never()).genererSystemFakta(anyString(), anyLong());
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedUgyldigDatoVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                            .medKey("informasjonsside.stonad.bostotte")
                            .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", "NaN-aN-aN")
                                .medProperty("tom", "NaN-aN-aN"));
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalSetteDelstegTilUtfyllingVedNullVerdiForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                                .medKey("informasjonsside.stonad.bostotte")
                                .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", null)
                                .medProperty("tom", null)
                );
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalIkkeSetteDelstegTilUtfyllingVedGyldigeDatoVerdierForTilleggsStonader() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SKJEMANUMMER_TILLEGGSSTONAD.get(0))
                .medId(1L)
                .medFaktum(
                        new Faktum()
                                .medKey("informasjonsside.stonad.bostotte")
                                .medValue("true")
                )
                .medFaktum(
                        new Faktum()
                                .medKey("bostotte.samling")
                                .medProperty("fom", "2017-01-01")
                                .medProperty("tom", "2017-02-02"));
        soknad = soknadServiceUtil.sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);
        assertThat(soknad.getDelstegStatus()).isNotEqualTo(DelstegStatus.UTFYLLING);
    }

    private static List<Vedlegg> mockHentVedleggForventninger(WebSoknad soknad) {

        List<Vedlegg> vedleggForventninger = soknad.getVedlegg();
        Vedlegg kvittering = KVITTERING_REF;
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
    }

}