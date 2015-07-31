package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.StartDatoService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceUtilTest {

    @Mock(name="lokalDb")
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
    private StartDatoService startDatoService;

    @Captor
    ArgumentCaptor<XMLHovedskjema> argument;

    @InjectMocks
    private SoknadServiceUtil soknadServiceUtil;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(lokalDb.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
        when(config.getSoknadBolker(any(WebSoknad.class), any(List.class))).thenReturn(new ArrayList());
        when(config.hentStruktur(any(Long.class))).thenReturn(new SoknadStruktur());
        when(kravdialogInformasjonHolder.hentAlleSkjemanumre()).thenReturn(new KravdialogInformasjonHolder().hentAlleSkjemanumre());
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
        when(lokalDb.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.hentFaktum(anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(startDatoService.erJanuarEllerFebruar()).thenReturn(true);
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        when(lokalDb.hentSoknadMedData(soknadId)).thenReturn(new WebSoknad().medId(soknadId));
        soknadServiceUtil.startSoknad(DAGPENGER);

        verify(faktaService, times(1)).lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum);
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
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(asList(behandlingsKjedeElement));
        when(henvendelsesConnector.startEttersending(orginalInnsending)).thenReturn(ettersendingsBehandlingId);

        Long soknadId = 11L;
        Faktum soknadInnsendingsDatoFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendingsDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
        when(lokalDb.hentFaktum(anyLong())).thenReturn(soknadInnsendingsDatoFaktum);
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        when(lokalDb.hentSoknadMedData(soknadId)).thenReturn(new WebSoknad().medId(soknadId));

        String ettersendingBehandlingsId = soknadServiceUtil.startEttersending(behandlingsId);
        verify(faktaService).lagreSystemFaktum(anyLong(), any(Faktum.class));
        assertNotNull(ettersendingBehandlingsId);
    }

    @Test
    public void skalStarteSoknad() {
        final long soknadId = 69L;
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString())).thenReturn("123");
        when(lokalDb.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.hentFaktum(anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        when(lokalDb.hentSoknadMedData(soknadId)).thenReturn(new WebSoknad().medId(soknadId));
        soknadServiceUtil.startSoknad(DAGPENGER);

        ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
        String bruker = StaticSubjectHandler.getSubjectHandler().getUid();
        verify(henvendelsesConnector).startSoknad(eq(bruker), eq(DAGPENGER), uid.capture());
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
        verify(lokalDb, atLeastOnce()).lagreFaktum(anyLong(), any(Faktum.class));
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
        when(lokalDb.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum().medFaktumId(1L));
        when(lokalDb.hentFaktum(anyLong())).thenReturn(new Faktum().medFaktumId(1L));
        when(startDatoService.erJanuarEllerFebruar()).thenReturn(false);
        when(lokalDb.opprettSoknad(any(WebSoknad.class))).thenReturn(soknadId);
        when(lokalDb.hentSoknadMedData(soknadId)).thenReturn(new WebSoknad().medId(soknadId));
        soknadServiceUtil.startSoknad(DAGPENGER);

        verify(faktaService, times(1)).lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum);
        DateTimeUtils.setCurrentMillisSystem();
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
        when(henvendelsesConnector.hentBehandlingskjede(behandlingsId)).thenReturn(asList(behandlingskjedeElement));
        when(henvendelsesConnector.hentSoknad(behandlingsId)).thenReturn(orginalInnsending);

        soknadServiceUtil.startEttersending(behandlingsId);
    }

}