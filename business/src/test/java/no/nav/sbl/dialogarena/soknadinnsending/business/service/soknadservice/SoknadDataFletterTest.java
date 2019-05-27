package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadDataFletterTest {

    public static final String SKJEMA_NUMMER = "NAV 04-01.03";
    private static final Vedlegg KVITTERING_REF = new Vedlegg()
            .medFillagerReferanse("kvitteringRef")
            .medSkjemaNummer(Kodeverk.KVITTERING)
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medStorrelse(3L)
            .medAntallSider(1);

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private HendelseRepository hendelseRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private OppgaveHandterer oppgaveHandterer;
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
    private VedleggRepository vedleggRepository;
    @Mock
    private SystemdataUpdater systemdataUpdater;
    @Mock
    ApplicationContext applicationContex;
    @Mock
    SoknadMetricsService soknadMetricsService;

    @Mock
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @InjectMocks
    private SoknadDataFletter soknadServiceUtil;

    @InjectMocks
    private EkstraMetadataService ekstraMetadataService;


    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        soknadServiceUtil.ekstraMetadataService = ekstraMetadataService;
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        when(lokalDb.hentSoknadType(anyLong())).thenReturn(SosialhjelpInformasjon.SKJEMANUMMER);
        when(lokalDb.hentLedigeFaktumIder(1)).thenReturn(Arrays.asList(1L));
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
        soknadServiceUtil.startSoknad(SosialhjelpInformasjon.SKJEMANUMMER);

        ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
        String bruker = OidcFeatureToggleUtils.getUserId();
        verify(henvendelsesConnector).startSoknad(eq(bruker), eq(SosialhjelpInformasjon.SKJEMANUMMER), uid.capture(), Matchers.any(SoknadType.class));
        WebSoknad soknad = new WebSoknad()
                .medId(soknadId)
                .medBehandlingId("123")
                .medUuid(uid.getValue())
                .medskjemaNummer(SosialhjelpInformasjon.SKJEMANUMMER)
                .medAktorId(bruker)
                .medOppretteDato(new DateTime())
                .medStatus(UNDER_ARBEID)
                .medDelstegStatus(OPPRETTET);
        Faktum faktum = new Faktum()
                .medFaktumId(1L)
                .medKey("progresjon")
                .medValue("1")
                .medType(BRUKERREGISTRERT)
                .medSoknadId(soknadId);
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(faktum);
        soknad.setFakta(fakta);
        verify(lokalDb).opprettSoknad(soknad);
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    @Ignore("Denne må utvides med søknadsosialhjelp sine vedleggsforventninger og faktum.")
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
                .medskjemaNummer(SosialhjelpInformasjon.SKJEMANUMMER)
                .medFaktum(new Faktum().medKey("personalia"))
                .medVedlegg(vedlegg)
                ;
        when(lokalDb.hentSoknadMedVedlegg(behandlingsId)).thenReturn(
                webSoknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(webSoknad);

        when(vedleggRepository.hentVedleggForskjemaNummer(1L, null, Kodeverk.KVITTERING))
                .thenReturn(KVITTERING_REF);
        when(vedleggService.hentVedleggOgKvittering(webSoknad)).thenReturn(mockHentVedleggForventninger(webSoknad));

        when(kravdialogInformasjonHolder.hentKonfigurasjon(SKJEMA_NUMMER)).thenReturn(new KravdialogInformasjonHolder().hentKonfigurasjon(SosialhjelpInformasjon.SKJEMANUMMER));
        soknadServiceUtil.sendSoknad(behandlingsId);

        ArgumentCaptor<HovedskjemaMetadata> hovedCaptor = ArgumentCaptor.forClass(HovedskjemaMetadata.class);
        ArgumentCaptor<VedleggMetadataListe> vedleggCaptor = ArgumentCaptor.forClass(VedleggMetadataListe.class);
        verify(oppgaveHandterer).leggTilOppgave(eq(behandlingsId), eq("123456"));

        HovedskjemaMetadata capturedHoved = hovedCaptor.getValue();
        assertThat(capturedHoved.filUuid).isEqualTo("uidHovedskjema");
        assertThat(capturedHoved.filnavn).isEqualTo(SosialhjelpInformasjon.SKJEMANUMMER);
        assertThat(capturedHoved.mimetype).isEqualTo("application/pdf");
        assertThat(capturedHoved.filStorrelse).isEqualTo("3");
        assertThat(capturedHoved.alternativRepresentasjon.get(0).mimetype).isEqualTo("application/pdf-fullversjon");

        VedleggMetadataListe capturedVedlegg = vedleggCaptor.getValue();
        assertThat(capturedVedlegg.vedleggListe).hasSize(3);
        assertThat(capturedVedlegg.vedleggListe.get(0).filnavn).isEqualTo("Test Annet vedlegg");
        assertThat(capturedVedlegg.vedleggListe.get(2).skjema).isEqualTo(Kodeverk.KVITTERING);
    }

    @Test
    public void skalPopulereFraMetadataNaarSoknadIkkeFinnes() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medFillagerReferanse("uidVedlegg");
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medskjemaNummer(SKJEMA_NUMMER).medId(11L)
                .medVedlegg(asList(vedlegg)).medStatus(UNDER_ARBEID);

        SoknadMetadata metadata = new SoknadMetadata();
        metadata.status = UNDER_ARBEID;
        metadata.hovedskjema = new HovedskjemaMetadata();
        metadata.hovedskjema.filUuid = "uidHovedskjema";
        VedleggMetadata v = new VedleggMetadata();
        v.filUuid = "uidVedlegg";
        metadata.vedlegg.vedleggListe.add(v);

        when(henvendelsesConnector.hentSoknad("123", true)).thenReturn(metadata);
        when(lokalDb.hentSoknad("123")).thenReturn(null, soknad, soknad);
        when(lokalDb.hentSoknadMedVedlegg("123")).thenReturn(soknad, soknad);
        when(lokalDb.hentSoknadMedData(11L)).thenReturn(soknad);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(soknad, baos);
        when(fillagerService.hentFil("uidHovedskjema"))
                .thenReturn(baos.toByteArray());
        WebSoknad webSoknad = soknadServiceUtil.hentSoknad("123", true, false);
        soknadServiceUtil.hentSoknad("123", true, false);
        ArgumentCaptor<WebSoknad> captor = ArgumentCaptor.forClass(WebSoknad.class);
        verify(lokalDb, times(1)).populerFraStruktur(captor.capture());

        WebSoknad captured = captor.getValue();
        assertThat(captured.getVedlegg().get(0).getFillagerReferanse()).isEqualTo("uidVedlegg");

        assertThat(webSoknad.getSoknadId()).isEqualTo(11L);
    }

    @Test
    public void lagreSystemfakumSomDefinertForSoknadVedHenting() {
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId("123")
                .medskjemaNummer(SosialhjelpInformasjon.SKJEMANUMMER)
                .medId(1L);
        when(lokalDb.hentSoknad("123")).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        soknadServiceUtil.hentSoknad("123", true, true);
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