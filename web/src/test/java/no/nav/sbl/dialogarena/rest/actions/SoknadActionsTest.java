package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.config.SoknadActionsTestConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfoService;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SendingTilKommuneErMidlertidigUtilgjengeligException;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.util.Locale;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.SENDT_MED_DIGISOS_API;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadActionsTestConfig.class})
public class SoknadActionsTest {

    public static final String SOKNADINNSENDING_ETTERSENDING_URL = "/soknadinnsending/ettersending";
    public static final String SAKSOVERSIKT_URL = "/saksoversikt";
    public static final String TESTKOMMUNE = "2352";
    private String EIER;

    @Inject
    NavMessageSource tekster;
    @Inject
    SoknadService soknadService;
    @Inject
    OppgaveHandterer oppgaveHandterer;
    @Inject
    InnsendingService innsendingService;
    @Inject
    SystemdataUpdater systemdataUpdater;
    @Inject
    SoknadActions actions;
    @Inject
    DigisosApi digisosApi;
    @Inject
    KommuneInfoService kommuneInfoService;
    @Inject
    private Tilgangskontroll tilgangskontroll;
    @Inject
    SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    DigisosApiService digisosApiService;

    ServletContext context = mock(ServletContext.class);

    @Before
    public void setUp() {
        System.setProperty("soknadinnsending.ettersending.path", SOKNADINNSENDING_ETTERSENDING_URL);
        System.setProperty("saksoversikt.link.url", SAKSOVERSIKT_URL);
        System.setProperty("authentication.isRunningWithOidc", "true");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        reset(tekster);
        when(tekster.finnTekst(eq("sendtSoknad.sendEpost.epostSubject"), any(Object[].class), any(Locale.class))).thenReturn("Emne");
        when(context.getRealPath(anyString())).thenReturn("");
        EIER = SubjectHandler.getUserIdFromToken();
    }

    @After
    public void tearDown() {
        System.clearProperty("digisosapi.sending.alltidTilTestkommune.enable");
        System.clearProperty("digisosapi.sending.enable");

    }

    @Test
    public void sendSoknadMedSoknadUnderArbeidNullSkalKalleSoknadService() {
        String behandlingsId = "soknadUnderArbeidErNull";
        actions.sendSoknad(behandlingsId, context, "");

        verify(soknadService, times(1)).sendSoknad(eq(behandlingsId));
    }

    @Test
    public void sendSoknadMedSendingTilFiksDisabledSkalKalleSoknadService() {
        String behandlingsId = "SendingTilFiksDisabled";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);

        actions.sendSoknad(behandlingsId, context, "");

        verify(soknadService, times(1)).sendSoknad(eq(behandlingsId));
    }

    @Test
    public void sendEttersendelsePaaSvarutSoknadSkalKalleSoknadService() {
        String behandlingsId = "ettersendelsePaaSvarUtSoknad";
        String soknadBehandlingsId = "soknadSendtViaSvarUt";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.status = UNDER_ARBEID;
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(soknadMetadataRepository.hent(soknadBehandlingsId)).thenReturn(soknadMetadata);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");

        verify(soknadService, times(1)).sendSoknad(eq(behandlingsId));
    }

    @Test(expected = IllegalStateException.class)
    public void sendEttersendelsePaaSoknadUtenMetadataSkalGiException() {
        String behandlingsId = "ettersendelsePaaSoknadUtenMetadata";
        String soknadBehandlingsId = "soknadSendtViaSvarUt";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");
    }

    @Test(expected = IllegalStateException.class)
    public void sendEttersendelsePaaDigisosApiSoknadSkalGiException() {
        String behandlingsId = "ettersendelsePaaDigisosApiSoknad";
        String soknadBehandlingsId = "soknadSendtViaSvarUt";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.status = SENDT_MED_DIGISOS_API;
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(soknadMetadataRepository.hent(soknadBehandlingsId)).thenReturn(soknadMetadata);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");
    }

    @Test
    public void sendSoknadTilKommuneUtenKonfigurasjonSkalKalleSoknadService() {
        String behandlingsId = "kommuneUtenKonfigurasjon";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.MANGLER_KONFIGURASJON);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");

        verify(soknadService, times(1)).sendSoknad(eq(behandlingsId));
    }

    @Test
    public void sendSoknadTilKommuneMedSvarUtSkalKalleSoknadService() {
        String behandlingsId = "kommuneMedSvarUt";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");

        verify(soknadService, times(1)).sendSoknad(eq(behandlingsId));
    }

    @Test
    public void sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
        String behandlingsId = "kommuneMedFDA";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");

        verify(digisosApiService, times(1)).sendSoknad(eq(soknadUnderArbeid), any(), any());
    }

    @Test(expected = SendingTilKommuneErMidlertidigUtilgjengeligException.class)
    public void sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
        String behandlingsId = "kommuneMedMidlertidigFeil";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
        System.setProperty("digisosapi.sending.enable", "true");

        actions.sendSoknad(behandlingsId, context, "");

        verify(digisosApiService, times(0)).sendSoknad(eq(soknadUnderArbeid), any(), any());
    }

    @Test
    public void getKommunenummerOrMockMedMockEnableSkalReturnereMock() {
        System.setProperty("digisosapi.sending.alltidTilTestkommune.enable", "true");
        String kommunenummer = actions.getKommunenummerOrMock(new SoknadUnderArbeid());
        Assert.assertEquals(TESTKOMMUNE, kommunenummer);
    }

    @Test
    public void getKommunenummerOrMockUtenMockSkalIkkeReturnereMock() {
        String expectedKommunenummer = "1111";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().withKommunenummer(expectedKommunenummer);

        String kommunenummer = actions.getKommunenummerOrMock(soknadUnderArbeid);

        Assert.assertEquals(expectedKommunenummer, kommunenummer);
    }
}
