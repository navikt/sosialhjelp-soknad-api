//package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.TextService;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
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
//import java.util.ArrayList;
//import java.util.List;
//
//import static java.util.Arrays.asList;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class StudielanRessursTest {
//
//    private static final String BEHANDLINGSID = "123";
//    private static final String EIER = "123456789101";
//
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//
//    @Mock
//    private TextService textService;
//
//    @InjectMocks
//    private StudielanRessurs studielanRessurs;
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
//    void getStudielanSkalReturnereNull(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, null));
//
//        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);
//
//        assertThat(studielanFrontend.skalVises).isTrue();
//        assertThat(studielanFrontend.bekreftelse).isNull();
//    }
//
//    @Test
//    void getStudielanSkalReturnereBekreftetStudielan(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, true));
//
//        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);
//
//        assertThat(studielanFrontend.skalVises).isTrue();
//        assertThat(studielanFrontend.bekreftelse).isTrue();
//    }
//
//    @Test
//    void getStudielanSkalReturnereHarIkkeStudielan(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, false));
//
//        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);
//
//        assertThat(studielanFrontend.skalVises).isTrue();
//        assertThat(studielanFrontend.bekreftelse).isFalse();
//    }
//
//    @Test
//    void getStudielanSkalReturnereSkalIkkeVisesHvisIkkeStudent(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithErStudentStudielanBekreftelse(false, null));
//
//        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);
//
//        assertThat(studielanFrontend.skalVises).isFalse();
//        assertThat(studielanFrontend.bekreftelse).isNull();
//    }
//
//    @Test
//    void getStudielanSkalReturnereSkalIkkeVisesHvisStudentSporsmalIkkeBesvart(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithErStudentStudielanBekreftelse(null, null));
//
//        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);
//
//        assertThat(studielanFrontend.skalVises).isFalse();
//        assertThat(studielanFrontend.bekreftelse).isNull();
//    }
//
//    @Test
//    void putStudielanSkalSetteStudielanOgLeggeTilInntektstypen(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        StudielanFrontend studielanFrontend = new StudielanFrontend();
//        studielanFrontend.setBekreftelse(true);
//        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        List<JsonOkonomioversiktInntekt> inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getInntekt();
//        assertThat(inntekt.get(0).getType()).isEqualTo(STUDIELAN);
//        JsonOkonomibekreftelse studielan = bekreftelser.get(0);
//        assertThat(studielan.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(studielan.getType()).isEqualTo(STUDIELAN);
//        assertThat(studielan.getVerdi()).isTrue();
//    }
//
//    @Test
//    void putStudielanSkalSetteHarIkkeStudielanOgSletteInntektstypen(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        SoknadUnderArbeid soknad = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        ArrayList<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
//        inntekt.add(new JsonOkonomioversiktInntekt().withType(STUDIELAN));
//        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        StudielanFrontend studielanFrontend = new StudielanFrontend();
//        studielanFrontend.setBekreftelse(false);
//        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        List<JsonOkonomioversiktInntekt> jsonInntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getInntekt();
//        assertThat(jsonInntekt).isEmpty();
//        JsonOkonomibekreftelse studielan = bekreftelser.get(0);
//        assertThat(studielan.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(studielan.getType()).isEqualTo(STUDIELAN);
//        assertThat(studielan.getVerdi()).isFalse();
//    }
//
//    @Test
//    void getStudielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putStudielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        var studielanFrontend = new StudielanFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend));
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
//    private SoknadUnderArbeid createJsonInternalSoknadWithErStudentStudielanBekreftelse(Boolean erStudent, Boolean verdi) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().withBekreftelse(
//                asList(new JsonOkonomibekreftelse()
//                        .withKilde(JsonKilde.BRUKER)
//                        .withType(STUDIELAN)
//                        .withVerdi(verdi)));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning().setErStudent(erStudent);
//        return soknadUnderArbeid;
//    }
//}
