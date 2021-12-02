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
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt.VerdiRessurs.VerdierFrontend;
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
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY;
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
//class VerdiRessursTest {
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
//    private VerdiRessurs verdiRessurs;
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
//    void getVerdierSkalReturnereBekreftelseLikNullOgAltFalse(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//
//        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);
//
//        assertThat(verdierFrontend.bekreftelse).isNull();
//        assertThat(verdierFrontend.bolig).isFalse();
//        assertThat(verdierFrontend.campingvogn).isFalse();
//        assertThat(verdierFrontend.kjoretoy).isFalse();
//        assertThat(verdierFrontend.fritidseiendom).isFalse();
//        assertThat(verdierFrontend.annet).isFalse();
//        assertThat(verdierFrontend.beskrivelseAvAnnet).isNull();
//    }
//
//    @Test
//    void getVerdierSkalReturnereBekreftelserLikTrue(){
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY,
//                        VERDI_FRITIDSEIENDOM, VERDI_ANNET), null));
//
//        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);
//
//        assertThat(verdierFrontend.bekreftelse).isTrue();
//        assertThat(verdierFrontend.bolig).isTrue();
//        assertThat(verdierFrontend.campingvogn).isTrue();
//        assertThat(verdierFrontend.kjoretoy).isTrue();
//        assertThat(verdierFrontend.fritidseiendom).isTrue();
//        assertThat(verdierFrontend.annet).isTrue();
//        assertThat(verdierFrontend.beskrivelseAvAnnet).isNull();
//    }
//
//    @Test
//    void getVerdierSkalReturnereBeskrivelseAvAnnet(){
//        String beskrivelse = "Bestefars klokke";
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithVerdier(true, asList(VERDI_ANNET), beskrivelse));
//
//        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);
//
//        assertThat(verdierFrontend.bekreftelse).isTrue();
//        assertThat(verdierFrontend.annet).isTrue();
//        assertThat(verdierFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse);
//    }
//
//    @Test
//    void putVerdierSkalSetteAltFalseDersomManVelgerHarIkkeVerdier(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY,
//                        VERDI_ANNET), "Bestefars klokke"));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//        verdierFrontend.setBekreftelse(false);
//        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        assertThat(verdiBekreftelse.getVerdi()).isFalse();
//        assertThat(verdier.isEmpty()).isTrue();
//    }
//
//    @Test
//    void putVerdierSkalSetteAlleBekreftelserLikFalse(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN,
//                        VERDI_KJORETOY, VERDI_ANNET), "Bestefars klokke"));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//        verdierFrontend.setBekreftelse(false);
//        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getVerdi();
//        assertThat(verdiBekreftelse.getVerdi()).isFalse();
//        assertThat(verdier.isEmpty()).isTrue();
//        assertThat(beskrivelse).isBlank();
//    }
//
//    @Test
//    void putVerdierSkalSetteNoenBekreftelser(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//        verdierFrontend.setBekreftelse(true);
//        verdierFrontend.setBolig(true);
//        verdierFrontend.setCampingvogn(true);
//        verdierFrontend.setFritidseiendom(false);
//        verdierFrontend.setKjoretoy(false);
//        verdierFrontend.setAnnet(false);
//        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        assertThat(verdiBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(verdiBekreftelse.getType()).isEqualTo(BEKREFTELSE_VERDI);
//        assertThat(verdiBekreftelse.getVerdi()).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_BOLIG))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_CAMPINGVOGN))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_KJORETOY))).isFalse();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_FRITIDSEIENDOM))).isFalse();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_ANNET))).isFalse();
//    }
//
//    @Test
//    void putVerdierSkalSetteAlleBekreftelser(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//        verdierFrontend.setBekreftelse(true);
//        verdierFrontend.setBolig(true);
//        verdierFrontend.setCampingvogn(true);
//        verdierFrontend.setFritidseiendom(true);
//        verdierFrontend.setKjoretoy(true);
//        verdierFrontend.setAnnet(true);
//        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
//        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOversikt().getFormue();
//        assertThat(verdiBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(verdiBekreftelse.getType()).isEqualTo(BEKREFTELSE_VERDI);
//        assertThat(verdiBekreftelse.getVerdi()).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_KJORETOY))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_CAMPINGVOGN))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_BOLIG))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_FRITIDSEIENDOM))).isTrue();
//        assertThat(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_ANNET))).isTrue();
//    }
//
//    @Test
//    void putVerdierSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet(){
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithVerdier(true, asList(VERDI_ANNET), "Vinylplater"));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        VerdierFrontend verdierFrontend = new VerdierFrontend();
//        verdierFrontend.setBekreftelse(false);
//        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
//        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getVerdi();
//        assertThat(verdiBekreftelse.getVerdi()).isFalse();
//        assertThat(beskrivelse).isBlank();
//    }
//
//    @Test
//    void getVerdierSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> verdiRessurs.hentVerdier(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putVerdierSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        var verdierFrontend = new VerdierFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend));
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
//    private SoknadUnderArbeid createJsonInternalSoknadWithVerdier(Boolean harVerdier, List<String> verdiTyper, String beskrivelseAvAnnet) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        List<JsonOkonomioversiktFormue> verdier = new ArrayList<>();
//        for (String verdi: verdiTyper) {
//            verdier.add(new JsonOkonomioversiktFormue()
//                    .withKilde(JsonKilde.BRUKER)
//                    .withType(verdi)
//                    .withTittel("tittel"));
//        }
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
//                .withKilde(JsonKilde.BRUKER)
//                .withType(BEKREFTELSE_VERDI)
//                .withVerdi(harVerdier)));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setFormue(verdier);
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
//                new JsonOkonomibeskrivelserAvAnnet().withVerdi(beskrivelseAvAnnet));
//        return soknadUnderArbeid;
//    }
//}
