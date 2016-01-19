package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EttersendingServiceTest {

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private VedleggService vedleggService;
    @Mock
    private FaktaService faktaService;
    @Mock
    private WebSoknadConfig config;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Mock
    private PersonaliaService personaliaService;
    @Mock
    private BarnService barnService;
    @Mock
    private ArbeidsforholdService arbeidsforholdService;
    @Mock
    ApplicationContext applicationContex;

    @InjectMocks
    private SoknadDataFletter soknadServiceUtil;

    @InjectMocks
    private EttersendingService ettersendingService;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        Map<String, BolkService> bolker = new HashMap<>();
        bolker.put(PersonaliaService.class.getName(), personaliaService);
        bolker.put(BarnService.class.getName(), barnService);
        bolker.put(ArbeidsforholdService.class.getName(), arbeidsforholdService);
        when(applicationContex.getBeansOfType(BolkService.class)).thenReturn(bolker);

        soknadServiceUtil.initBolker();
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(lokalDb.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
        when(config.getSoknadBolker(any(WebSoknad.class), any(List.class))).thenReturn(new ArrayList());
        when(config.hentStruktur(any(Long.class))).thenReturn(new SoknadStruktur());
        when(kravdialogInformasjonHolder.hentAlleSkjemanumre()).thenReturn(new KravdialogInformasjonHolder().hentAlleSkjemanumre());
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

        String ettersendingBehandlingsId = ettersendingService.start(behandlingsId);
        verify(faktaService).lagreSystemFaktum(anyLong(), any(Faktum.class));
        assertNotNull(ettersendingBehandlingsId);
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

        ettersendingService.start(behandlingsId);
    }

    @Test
    public void skalKunLagreSystemfakumPersonaliaForEttersendingerVedHenting() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123")
                .medskjemaNummer(DAGPENGER)
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medId(1L);
        when(lokalDb.hentSoknad("123")).thenReturn(
                soknad);
        when(config.getSoknadBolker(any(WebSoknad.class), anyListOf(BolkService.class))).thenReturn(asList(personaliaService, barnService));
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(soknad);
        when(lokalDb.hentSoknadMedData(1L)).thenReturn(soknad);
        soknadServiceUtil.hentSoknad("123", true, true);
        verify(personaliaService, times(1)).genererSystemFakta(anyString(), anyLong());
        verify(barnService, never()).genererSystemFakta(anyString(), anyLong());
    }
}