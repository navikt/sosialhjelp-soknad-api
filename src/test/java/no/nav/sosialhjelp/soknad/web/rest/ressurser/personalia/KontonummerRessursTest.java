//package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.systemdata.KontonummerSystemdata;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.KontonummerRessurs.KontonummerFrontend;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doCallRealMethod;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class KontonummerRessursTest {
//
//    private static final String BEHANDLINGSID = "123";
//    private static final String EIER = "123456789101";
//    private static final String KONTONUMMER_BRUKER = "11122233344";
//    private static final String KONTONUMMER_SYSTEM = "44333222111";
//
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    @Mock
//    private KontonummerSystemdata kontonummerSystemdata;
//
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//
//    @InjectMocks
//    private KontonummerRessurs kontonummerRessurs;
//
//    @BeforeEach
//    public void setUp() {
//        System.setProperty("environment.name", "test");
//        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
//    }
//
//    @AfterEach
//    public void tearDown() {
//        SubjectHandler.resetOidcSubjectHandlerService();
//        System.clearProperty("environment.name");
//    }
//
//    @Test
//    void getKontonummerSkalReturnereSystemKontonummer() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, KONTONUMMER_SYSTEM));
//
//        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);
//
//        assertThat(kontonummerFrontend.brukerutfyltVerdi).isNull();
//        assertThat(kontonummerFrontend.systemverdi).isEqualTo(KONTONUMMER_SYSTEM);
//        assertThat(kontonummerFrontend.harIkkeKonto).isNull();
//        assertThat(kontonummerFrontend.brukerdefinert).isFalse();
//    }
//
//    @Test
//    void getKontonummerSkalReturnereBrukerutfyltKontonummer() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER));
//        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);
//
//        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);
//
//        assertThat(kontonummerFrontend.brukerutfyltVerdi).isEqualTo(KONTONUMMER_BRUKER);
//        assertThat(kontonummerFrontend.systemverdi).isEqualTo(KONTONUMMER_SYSTEM);
//        assertThat(kontonummerFrontend.harIkkeKonto).isNull();
//        assertThat(kontonummerFrontend.brukerdefinert).isTrue();
//    }
//
//    @Test
//    void getKontonummerSkalReturnereKontonummerLikNull() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, null));
//        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(null);
//
//        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);
//
//        assertThat(kontonummerFrontend.brukerutfyltVerdi).isNull();
//        assertThat(kontonummerFrontend.systemverdi).isNull();
//        assertThat(kontonummerFrontend.harIkkeKonto).isNull();
//        assertThat(kontonummerFrontend.brukerdefinert).isTrue();
//    }
//
//    @Test
//    void putKontonummerSkalSetteBrukerutfyltKontonummer() {
//        startWithEmptyKontonummerAndNoSystemKontonummer();
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        final KontonummerFrontend kontonummerFrontend = new KontonummerFrontend()
//                .withBrukerdefinert(true)
//                .withBrukerutfyltVerdi(KONTONUMMER_BRUKER);
//        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
//        assertThat(kontonummer.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(kontonummer.getHarIkkeKonto()).isNull();
//        assertThat(kontonummer.getVerdi()).isEqualTo(KONTONUMMER_BRUKER);
//    }
//
//    @Test
//    void putKontonummerSkalOverskriveBrukerutfyltKontonummerMedSystemKontonummer() {
//        startWithBrukerKontonummerAndSystemKontonummerInTPS();
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        doCallRealMethod().when(kontonummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class), any());
//
//        final KontonummerFrontend kontonummerFrontend = new KontonummerFrontend()
//                .withBrukerdefinert(false)
//                .withSystemverdi(KONTONUMMER_SYSTEM);
//        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
//        assertThat(kontonummer.getKilde()).isEqualTo(JsonKilde.SYSTEM);
//        assertThat(kontonummer.getHarIkkeKonto()).isNull();
//        assertThat(kontonummer.getVerdi()).isEqualTo(KONTONUMMER_SYSTEM);
//    }
//
//    @Test
//    void getKontonummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> kontonummerRessurs.hentKontonummer(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putKontonummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        var kontonummerFrontend = new KontonummerFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
//        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
//        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
//        return argument.getValue();
//    }
//
//    private void startWithBrukerKontonummerAndSystemKontonummerInTPS() {
//        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER));
//    }
//
//    private void startWithEmptyKontonummerAndNoSystemKontonummer() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, null));
//    }
//
//    private SoknadUnderArbeid createJsonInternalSoknadWithKontonummer(JsonKilde kilde, String verdi) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer()
//                .withKilde(kilde)
//                .withVerdi(verdi);
//        return soknadUnderArbeid;
//    }
//
//}
