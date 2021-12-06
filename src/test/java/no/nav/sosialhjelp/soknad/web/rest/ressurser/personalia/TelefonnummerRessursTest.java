//package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.systemdata.TelefonnummerSystemdata;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.TelefonnummerRessurs.TelefonnummerFrontend;
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
//class TelefonnummerRessursTest {
//
//    private static final String BEHANDLINGSID = "123";
//    private static final String EIER = "123456789101";
//    private static final String TELEFONNUMMER_BRUKER = "98765432";
//    private static final String TELEFONNUMMER_SYSTEM = "23456789";
//
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    @Mock
//    private TelefonnummerSystemdata telefonnummerSystemdata;
//
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//
//    @InjectMocks
//    private TelefonnummerRessurs telefonnummerRessurs;
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
//    void getTelefonnummerSkalReturnereSystemTelefonnummer(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(JsonKilde.SYSTEM, TELEFONNUMMER_SYSTEM));
//
//        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);
//
//        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isNull();
//        assertThat(telefonnummerFrontend.systemverdi).isEqualTo(TELEFONNUMMER_SYSTEM);
//        assertThat(telefonnummerFrontend.brukerdefinert).isFalse();
//    }
//
//    @Test
//    void getTelefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(null, null));
//        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(null);
//
//        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);
//
//        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isNull();
//        assertThat(telefonnummerFrontend.systemverdi).isNull();
//        assertThat(telefonnummerFrontend.brukerdefinert).isTrue();
//    }
//
//    @Test
//    void getTelefonnummerSkalReturnereBrukerutfyltTelefonnummer(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER));
//        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);
//
//        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);
//
//        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isEqualTo(TELEFONNUMMER_BRUKER);
//        assertThat(telefonnummerFrontend.systemverdi).isEqualTo(TELEFONNUMMER_SYSTEM);
//        assertThat(telefonnummerFrontend.brukerdefinert).isTrue();
//    }
//
//    @Test
//    void putTelefonnummerSkalLageNyJsonTelefonnummerDersomDenVarNull(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(null, null));
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
//                .withBrukerdefinert(true)
//                .withBrukerutfyltVerdi(TELEFONNUMMER_BRUKER);
//        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
//        assertThat(telefonnummer.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(telefonnummer.getVerdi()).isEqualTo(TELEFONNUMMER_BRUKER);
//    }
//
//    @Test
//    void putTelefonnummerSkalOppdatereBrukerutfyltTelefonnummer(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(null, null));
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
//                .withBrukerdefinert(true)
//                .withBrukerutfyltVerdi(TELEFONNUMMER_BRUKER);
//        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
//        assertThat(telefonnummer.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(telefonnummer.getVerdi()).isEqualTo(TELEFONNUMMER_BRUKER);
//    }
//
//    @Test
//    void putTelefonnummerSkalOverskriveBrukerutfyltTelefonnummerMedSystemTelefonnummer(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER));
//        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        doCallRealMethod().when(telefonnummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class), any());
//
//        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
//                .withBrukerdefinert(false);
//        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
//        assertThat(telefonnummer.getKilde()).isEqualTo(JsonKilde.SYSTEM);
//        assertThat(telefonnummer.getVerdi()).isEqualTo(TELEFONNUMMER_SYSTEM);
//    }
//
//    @Test
//    void getTelefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putTelefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        var telefonnummerFrontend = new TelefonnummerFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend));
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
//    private SoknadUnderArbeid createJsonInternalSoknadWithTelefonnummer(JsonKilde kilde, String verdi) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
//                .withTelefonnummer(verdi == null ? null : new JsonTelefonnummer()
//                        .withKilde(kilde)
//                        .withVerdi(verdi));
//        return soknadUnderArbeid;
//    }
//}
