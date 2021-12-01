//package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
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
//class FormueRessursTest {
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
//    private FormueRessurs formueRessurs;
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
//    void getFormueSkalReturnereBekreftelserLikFalse() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//
//        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);
//
//        assertThat(formueFrontend.brukskonto).isFalse();
//        assertThat(formueFrontend.bsu).isFalse();
//        assertThat(formueFrontend.livsforsikring).isFalse();
//        assertThat(formueFrontend.sparekonto).isFalse();
//        assertThat(formueFrontend.verdipapirer).isFalse();
//        assertThat(formueFrontend.annet).isFalse();
//        assertThat(formueFrontend.beskrivelseAvAnnet).isNull();
//    }
//
//    @Test
//    void getFormueSkalReturnereBekreftelserLikTrue() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithFormue(asList(FORMUE_BRUKSKONTO, FORMUE_BSU,
//                        FORMUE_LIVSFORSIKRING, FORMUE_VERDIPAPIRER, FORMUE_SPAREKONTO, FORMUE_ANNET), null));
//
//        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);
//
//        assertThat(formueFrontend.brukskonto).isTrue();
//        assertThat(formueFrontend.bsu).isTrue();
//        assertThat(formueFrontend.livsforsikring).isTrue();
//        assertThat(formueFrontend.sparekonto).isTrue();
//        assertThat(formueFrontend.verdipapirer).isTrue();
//        assertThat(formueFrontend.annet).isTrue();
//        assertThat(formueFrontend.beskrivelseAvAnnet).isNull();
//    }
//
//    @Test
//    void getFormueSkalReturnereBeskrivelseAvAnnet() {
//        String beskrivelse = "Vinylplater";
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithFormue(asList(FORMUE_ANNET), beskrivelse));
//
//        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);
//
//        assertThat(formueFrontend.annet).isTrue();
//        assertThat(formueFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse);
//    }
//
//    @Test
//    void putFormueSkalSetteAlleBekreftelserLikFalse() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithFormue(asList(FORMUE_BRUKSKONTO, FORMUE_BSU,
//                        FORMUE_LIVSFORSIKRING, FORMUE_VERDIPAPIRER, FORMUE_SPAREKONTO, FORMUE_ANNET), "Vinylplater"));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        FormueFrontend formueFrontend = new FormueFrontend();
//        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getSparing();
//        assertThat(sparing.getVerdi()).isFalse();
//        assertThat(formuer).isEmpty();
//        assertThat(beskrivelse).isBlank();
//    }
//
//    @Test
//    void putFormueSkalSetteNoenBekreftelser() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        FormueFrontend formueFrontend = new FormueFrontend();
//        formueFrontend.setBrukskonto(true);
//        formueFrontend.setBsu(true);
//        formueFrontend.setLivsforsikring(true);
//        formueFrontend.setSparekonto(false);
//        formueFrontend.setVerdipapirer(false);
//        formueFrontend.setAnnet(false);
//        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        assertThat(sparing.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(sparing.getType()).isEqualTo(BEKREFTELSE_SPARING);
//        assertThat(sparing.getVerdi()).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_BRUKSKONTO))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_BSU))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_LIVSFORSIKRING))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_SPAREKONTO))).isFalse();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_VERDIPAPIRER))).isFalse();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_ANNET))).isFalse();
//    }
//
//    @Test
//    void putFormueSkalSetteAlleBekreftelser() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        FormueFrontend formueFrontend = new FormueFrontend();
//        formueFrontend.setBrukskonto(true);
//        formueFrontend.setBsu(true);
//        formueFrontend.setLivsforsikring(true);
//        formueFrontend.setSparekonto(true);
//        formueFrontend.setVerdipapirer(true);
//        formueFrontend.setAnnet(true);
//        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        assertThat(sparing.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(sparing.getType()).isEqualTo(BEKREFTELSE_SPARING);
//        assertThat(sparing.getVerdi()).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_BRUKSKONTO))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_BSU))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_LIVSFORSIKRING))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_SPAREKONTO))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_VERDIPAPIRER))).isTrue();
//        assertThat(formuer.stream().anyMatch(formue -> formue.getType().equals(FORMUE_ANNET))).isTrue();
//    }
//
//    @Test
//    void putFormueSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithFormue(asList(FORMUE_ANNET), "Vinylplater"));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        FormueFrontend formueFrontend = new FormueFrontend();
//        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
//        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getSparing();
//        assertThat(sparing.getVerdi()).isFalse();
//        assertThat(beskrivelse).isBlank();
//    }
//
//    @Test
//    void getFormueSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> formueRessurs.hentFormue(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putFormueSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        var formueFrontend = new FormueFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend));
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
//    private SoknadUnderArbeid createJsonInternalSoknadWithFormue(List<String> formueTyper, String beskrivelseAvAnnet) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        List<JsonOkonomioversiktFormue> formuer = new ArrayList<>();
//        for (String formue : formueTyper) {
//            formuer.add(new JsonOkonomioversiktFormue()
//                    .withKilde(JsonKilde.BRUKER)
//                    .withType(formue)
//                    .withTittel("tittel"));
//        }
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setFormue(formuer);
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
//                new JsonOkonomibeskrivelserAvAnnet().withSparing(beskrivelseAvAnnet));
//        return soknadUnderArbeid;
//    }
//}
