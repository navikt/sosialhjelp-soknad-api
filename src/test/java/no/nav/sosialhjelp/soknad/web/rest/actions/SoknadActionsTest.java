//package no.nav.sosialhjelp.soknad.web.rest.actions;
//
//import no.finn.unleash.Unleash;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErIkkeAktivertException;
//import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.business.exceptions.SoknadenHarNedetidException;
//import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
//import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
//import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService;
//import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import no.nav.sosialhjelp.soknad.web.utils.NedetidUtils;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import javax.servlet.ServletContext;
//import java.time.LocalDateTime;
//
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API;
//import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.UNDER_ARBEID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@MockitoSettings(strictness = Strictness.LENIENT)
//class SoknadActionsTest {
//
//    public static final String TESTKOMMUNE = "3002";
//    public static final String KOMMUNE_I_SVARUT_LISTEN = "0301";
//    private String EIER;
//
//    @Mock
//    private SoknadService soknadService;
//    @Mock
//    private KommuneInfoService kommuneInfoService;
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    @Mock
//    private SoknadMetadataRepository soknadMetadataRepository;
//    @Mock
//    private DigisosApiService digisosApiService;
//    @Mock
//    private Unleash unleash;
//
//    @InjectMocks
//    private SoknadActions actions;
//
//    ServletContext context = mock(ServletContext.class);
//
//    @BeforeEach
//    public void setUp() {
//        System.setProperty("environment.name", "test");
//        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
//        when(context.getRealPath(anyString())).thenReturn("");
//        EIER = SubjectHandler.getUserId();
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//    }
//
//    @AfterEach
//    public void tearDown() {
//        System.clearProperty("digisosapi.sending.alltidTilTestkommune.enable");
//        System.clearProperty("digisosapi.sending.enable");
//        System.clearProperty(NedetidUtils.NEDETID_START);
//        System.clearProperty(NedetidUtils.NEDETID_SLUTT);
//        SubjectHandler.resetOidcSubjectHandlerService();
//        System.clearProperty("environment.name");
//    }
//
//    @Test
//    void sendSoknadINedetidSkalKasteException() {
//        System.setProperty(NedetidUtils.NEDETID_START, LocalDateTime.now().minusDays(1).format(NedetidUtils.dateTimeFormatter));
//        System.setProperty(NedetidUtils.NEDETID_SLUTT, LocalDateTime.now().plusDays(2).format(NedetidUtils.dateTimeFormatter));
//
//        assertThatExceptionOfType(SoknadenHarNedetidException.class)
//                .isThrownBy(() -> actions.sendSoknad("behandlingsId", context, ""));
//
////        verify(soknadService, times(0)).sendSoknad(any());
////        verify(digisosApiService, times(0)).sendSoknad(any(), any(), any());
//    }
//
//    @Test
//    void sendSoknadMedSoknadUnderArbeidNullSkalKasteException() {
//        String behandlingsId = "soknadUnderArbeidErNull";
//
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> actions.sendSoknad("behandlingsId", context, ""));
//
//        verify(soknadService, times(0)).sendSoknad(behandlingsId);
//        verify(digisosApiService, times(0)).sendSoknad(any(), any(), any());
//    }
//
//    @Test
//    void sendSoknadMedSendingTilFiksDisabledSkalKalleSoknadService() {
//        String behandlingsId = "SendingTilFiksDisabled";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//
//        actions.sendSoknad(behandlingsId, context, "");
//
//        verify(soknadService, times(1)).sendSoknad(behandlingsId);
//    }
//
//    @Test
//    void sendEttersendelsePaaSvarutSoknadSkalKalleSoknadService() {
//        String behandlingsId = "ettersendelsePaaSvarUtSoknad";
//        String soknadBehandlingsId = "soknadSendtViaSvarUt";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
//        SoknadMetadata soknadMetadata = new SoknadMetadata();
//        soknadMetadata.status = UNDER_ARBEID;
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(soknadMetadataRepository.hent(soknadBehandlingsId)).thenReturn(soknadMetadata);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        actions.sendSoknad(behandlingsId, context, "");
//
//        verify(soknadService, times(1)).sendSoknad(behandlingsId);
//    }
//
//    @Test
//    void sendEttersendelsePaaSoknadUtenMetadataSkalGiException() {
//        String behandlingsId = "ettersendelsePaaSoknadUtenMetadata";
//        String soknadBehandlingsId = "soknadSendtViaSvarUt";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> actions.sendSoknad(behandlingsId, context, ""));
//    }
//
//    @Test
//    void sendEttersendelsePaaDigisosApiSoknadSkalGiException() {
//        String behandlingsId = "ettersendelsePaaDigisosApiSoknad";
//        String soknadBehandlingsId = "soknadSendtViaSvarUt";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withTilknyttetBehandlingsId(soknadBehandlingsId);
//        SoknadMetadata soknadMetadata = new SoknadMetadata();
//        soknadMetadata.status = SENDT_MED_DIGISOS_API;
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(soknadMetadataRepository.hent(soknadBehandlingsId)).thenReturn(soknadMetadata);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> actions.sendSoknad(behandlingsId, context, ""));
//    }
//
//    @Test
//    void sendSoknadMedFiksNedetidOgTomCacheSkalKasteException() {
//        String behandlingsId = "fiksNedetidOgTomCache";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().setKommunenummer(KOMMUNE_I_SVARUT_LISTEN);
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException.class)
//                .isThrownBy(() -> actions.sendSoknad(behandlingsId, context, ""));
//
////        verify(soknadService, times(1)).sendSoknad(behandlingsId);
//    }
//
//    @Test
//    void sendSoknadTilKommuneUtenKonfigurasjonSkalKalleSoknadService() {
//        String behandlingsId = "kommuneUtenKonfigurasjon";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().setKommunenummer(KOMMUNE_I_SVARUT_LISTEN);
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.MANGLER_KONFIGURASJON);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        actions.sendSoknad(behandlingsId, context, "");
//
//        verify(soknadService, times(1)).sendSoknad(behandlingsId);
//    }
//
//    @Test
//    void sendSoknadTilKommuneMedSvarUtSkalKalleSoknadService() {
//        String behandlingsId = "kommuneMedSvarUt";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().setKommunenummer(KOMMUNE_I_SVARUT_LISTEN);
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any(String.class))).thenReturn(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        actions.sendSoknad(behandlingsId, context, "");
//
//        verify(soknadService, times(1)).sendSoknad(behandlingsId);
//    }
//
//    @Test
//    void sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
//        String behandlingsId = "kommuneMedFDA";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any())).thenReturn(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        actions.sendSoknad(behandlingsId, context, "");
//
//        verify(digisosApiService, times(1)).sendSoknad(eq(soknadUnderArbeid), any(), any());
//    }
//
//    @Test
//    void sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
//        String behandlingsId = "kommuneMedMidlertidigFeil";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any())).thenReturn(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException.class)
//                .isThrownBy(() -> actions.sendSoknad(behandlingsId, context, ""));
//
//        verify(digisosApiService, times(0)).sendSoknad(eq(soknadUnderArbeid), any(), any());
//    }
//
//    @Test
//    void sendSoknadTilKommuneSomIkkeErAktivertEllerSvarUtSkalKasteException() {
//        String behandlingsId = "kommueMedMottakDeaktivertOgIkkeSvarut";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().setKommunenummer("9999_kommune_uten_svarut");
//        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER)).thenReturn(soknadUnderArbeid);
//        when(kommuneInfoService.kommuneInfo(any())).thenReturn(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);
//        System.setProperty("digisosapi.sending.enable", "true");
//
//        assertThatExceptionOfType(SendingTilKommuneErIkkeAktivertException.class)
//                .isThrownBy(() -> actions.sendSoknad(behandlingsId, context, ""));
//
//        verify(digisosApiService, times(0)).sendSoknad(eq(soknadUnderArbeid), any(), any());
//    }
//
//    @Test
//    void getKommunenummerOrMockMedMockEnableSkalReturnereMock() {
//        System.setProperty("digisosapi.sending.alltidTilTestkommune.enable", "true");
//        String kommunenummer = actions.getKommunenummerOrMock(new SoknadUnderArbeid());
//        assertThat(kommunenummer).isEqualTo(TESTKOMMUNE);
//    }
//
//    @Test
//    void getKommunenummerOrMockUtenMockSkalIkkeReturnereMock() {
//        String expectedKommunenummer = "1111";
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().withKommunenummer(expectedKommunenummer);
//
//        String kommunenummer = actions.getKommunenummerOrMock(soknadUnderArbeid);
//
//        assertThat(kommunenummer).isEqualTo(expectedKommunenummer);
//    }
//
//    @Test
//    void sendSoknadSkalGiAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> actions.sendSoknad("behandlingsId", mock(ServletContext.class), "token"));
//
////        verifyNoInteractions(soknadService);
////        verifyNoInteractions(kommuneInfoService);
////        verifyNoInteractions(digisosApiService);
//    }
//}
