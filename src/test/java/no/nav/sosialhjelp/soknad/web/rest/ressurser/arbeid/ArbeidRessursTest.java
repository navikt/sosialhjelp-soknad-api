//package no.nav.sosialhjelp.soknad.web.rest.ressurser.arbeid;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
//import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.systemdata.ArbeidsforholdSystemdata;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.arbeid.ArbeidRessurs.ArbeidFrontend;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.arbeid.ArbeidRessurs.ArbeidsforholdFrontend;
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
//class ArbeidRessursTest {
//
//    private static final String BEHANDLINGSID = "123";
//    private static final String EIER = "123456789101";
//    private static final String KOMMENTAR = "Hath not the potter power over the clay, to make one vessel unto honor and another unto dishonor?";
//
//    private static final JsonArbeidsforhold ARBEIDSFORHOLD_1 = new JsonArbeidsforhold()
//            .withKilde(JsonKilde.SYSTEM)
//            .withArbeidsgivernavn("Good Corp.")
//            .withFom("1337-01-01")
//            .withTom("2020-01-01")
//            .withStillingstype(JsonArbeidsforhold.Stillingstype.FAST)
//            .withStillingsprosent(50)
//            .withOverstyrtAvBruker(Boolean.FALSE);
//
//    private static final JsonArbeidsforhold ARBEIDSFORHOLD_2 = new JsonArbeidsforhold()
//                .withKilde(JsonKilde.SYSTEM)
//                .withArbeidsgivernavn("Evil Corp.")
//                .withFom("1337-02-02")
//                .withTom("2020-02-02")
//                .withStillingstype(JsonArbeidsforhold.Stillingstype.VARIABEL)
//                .withStillingsprosent(30)
//            .withOverstyrtAvBruker(Boolean.FALSE);
//
//    @Mock
//    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;
//
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//
//    @InjectMocks
//    private ArbeidRessurs arbeidRessurs;
//
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
//    void getArbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(createArbeidsforholdListe(), null));
//
//        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);
//        final List<ArbeidsforholdFrontend> arbeidsforholdFrontends = arbeidFrontend.arbeidsforhold;
//
//        assertThat(arbeidsforholdFrontends).hasSize(2);
//        final ArbeidsforholdFrontend arbeidsforhold_1 = arbeidsforholdFrontends.get(0);
//        final ArbeidsforholdFrontend arbeidsforhold_2 = arbeidsforholdFrontends.get(1);
//
//        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_1, ARBEIDSFORHOLD_1);
//        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_2, ARBEIDSFORHOLD_2);
//    }
//
//    @Test
//    void getArbeidSkalReturnereArbeidsforholdLikNull(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, null));
//
//        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);
//
//        assertThat(arbeidFrontend.arbeidsforhold).isNull();
//    }
//
//    @Test
//    void getArbeidSkalReturnereKommentarTilArbeidsforholdLikNull(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, null));
//
//        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);
//
//        assertThat(arbeidFrontend.kommentarTilArbeidsforhold).isNull();
//    }
//
//    @Test
//    void getArbeidSkalReturnereKommentarTilArbeidsforhold(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, KOMMENTAR));
//
//        final ArbeidFrontend arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID);
//
//        assertThat(arbeidFrontend.kommentarTilArbeidsforhold).isEqualTo(KOMMENTAR);
//    }
//
//    @Test
//    void putArbeidSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, null));
//
//        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold(KOMMENTAR);
//        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
//        assertThat(kommentarTilArbeidsforhold.getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
//        assertThat(kommentarTilArbeidsforhold.getVerdi()).isEqualTo(KOMMENTAR);
//    }
//
//    @Test
//    void putArbeidSkalOppdatereKommentarTilArbeidsforhold(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar"));
//
//        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold(KOMMENTAR);
//        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
//        assertThat(kommentarTilArbeidsforhold.getKilde()).isEqualTo(JsonKildeBruker.BRUKER);
//        assertThat(kommentarTilArbeidsforhold.getVerdi()).isEqualTo(KOMMENTAR);
//    }
//
//    @Test
//    void putArbeidSkalSetteLikNullDersomKommentarenErTom(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar"));
//
//        final ArbeidFrontend arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold("");
//        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend);
//
//        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        final JsonKommentarTilArbeidsforhold kommentarTilArbeidsforhold = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getKommentarTilArbeidsforhold();
//        assertThat(kommentarTilArbeidsforhold).isNull();
//    }
//
//    @Test
//    void getArbeidSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> arbeidRessurs.hentArbeid(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putArbeidSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        var arbeidFrontend = new ArbeidFrontend().withKommentarTilArbeidsforhold("");
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend));
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
//    private void assertThatArbeidsforholdIsCorrectlyConverted(ArbeidsforholdFrontend forholdFrontend, JsonArbeidsforhold jsonForhold) {
//        assertThat(forholdFrontend.arbeidsgivernavn).isEqualTo(jsonForhold.getArbeidsgivernavn());
//        assertThat(forholdFrontend.fom).isEqualTo(jsonForhold.getFom());
//        assertThat(forholdFrontend.tom).isEqualTo(jsonForhold.getTom());
//        assertThat(forholdFrontend.stillingsprosent).isEqualTo(jsonForhold.getStillingsprosent());
//        assertThatStillingstypeIsCorrect(forholdFrontend.stillingstypeErHeltid, jsonForhold.getStillingstype());
//        assertThat(forholdFrontend.overstyrtAvBruker).isEqualTo(Boolean.FALSE);
//    }
//
//    private void assertThatStillingstypeIsCorrect(Boolean stillingstypeErHeltid, JsonArbeidsforhold.Stillingstype stillingstype){
//        if (stillingstypeErHeltid == null){
//            return;
//        }
//        if (stillingstypeErHeltid){
//            assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.FAST);
//        } else {
//            assertThat(stillingstype).isEqualTo(JsonArbeidsforhold.Stillingstype.VARIABEL);
//        }
//    }
//
//    private List<JsonArbeidsforhold> createArbeidsforholdListe(){
//        List<JsonArbeidsforhold> forholdListe = new ArrayList<>();
//        forholdListe.add(ARBEIDSFORHOLD_1);
//        forholdListe.add(ARBEIDSFORHOLD_2);
//        return forholdListe;
//    }
//
//    private SoknadUnderArbeid createJsonInternalSoknadWithArbeid(List<JsonArbeidsforhold> arbeidsforholdList, String kommentar) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid()
//                .withForhold(arbeidsforholdList)
//                .withKommentarTilArbeidsforhold(kommentar == null ? null : new JsonKommentarTilArbeidsforhold()
//                        .withKilde(JsonKildeBruker.BRUKER)
//                        .withVerdi(kommentar));
//        return soknadUnderArbeid;
//    }
//}
