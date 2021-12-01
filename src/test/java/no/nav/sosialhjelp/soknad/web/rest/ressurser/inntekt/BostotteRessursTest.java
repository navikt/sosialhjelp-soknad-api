//package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.TextService;
//import no.nav.sosialhjelp.soknad.business.service.systemdata.BostotteSystemdata;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs;
//import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs.BostotteFrontend;
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
//import java.util.Collections;
//import java.util.List;
//
//import static java.util.Arrays.asList;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
//import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class BostotteRessursTest {
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
//    private BostotteSystemdata bostotteSystemdata;
//
//    @Mock
//    private TextService textService;
//
//    @InjectMocks
//    private BostotteRessurs bostotteRessurs;
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
//    void getBostotteSkalReturnereNull() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.bekreftelse).isNull();
//    }
//
//    @Test
//    void getBostotteSkalReturnereBekreftetBostotte() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithBostotte(true));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.bekreftelse).isTrue();
//    }
//
//    @Test
//    void getBostotteSkalReturnereHarIkkeBostotte() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithBostotte(false));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.bekreftelse).isFalse();
//    }
//
//    @Test
//    void putBostotteSkalSetteBostotteOgLeggeTilInntektstypen() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        BostotteFrontend bostotteFrontend = new BostotteFrontend();
//        bostotteFrontend.setBekreftelse(true);
//        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend, "token");
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        List<JsonOkonomiOpplysningUtbetaling> utbetaling = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getUtbetaling();
//        assertThat(utbetaling.get(0).getType()).isEqualTo(UTBETALING_HUSBANKEN);
//        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
//        assertThat(bostotte.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(bostotte.getType()).isEqualTo(BOSTOTTE);
//        assertThat(bostotte.getVerdi()).isTrue();
//    }
//
//    @Test
//    void putBostotteSkalSetteHarIkkeBostotteOgSletteInntektstypen() {
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        SoknadUnderArbeid soknad = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        ArrayList<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
//        inntekt.add(new JsonOkonomioversiktInntekt().withType(BOSTOTTE));
//        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        BostotteFrontend bostotteFrontend = new BostotteFrontend();
//        bostotteFrontend.setBekreftelse(false);
//        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend, "token");
//
//        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getBekreftelse();
//        List<JsonOkonomiOpplysningUtbetaling> utbetaling = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
//                .getOkonomi().getOpplysninger().getUtbetaling();
//        assertThat(utbetaling).isEmpty();
//        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
//        assertThat(bostotte.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(bostotte.getType()).isEqualTo(BOSTOTTE);
//        assertThat(bostotte.getVerdi()).isFalse();
//    }
//
//    @Test
//    void bostotte_skalBareHaUtRiktigUtbetaling() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithBostotteUtbetalinger(true, asList("tilfeldig", "salg", "lonn")));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.utbetalinger).hasSize(1);
//    }
//
//    @Test
//    void bostotte_skalIkkeHaUtbetaling() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithBostotteUtbetalinger(false, asList("tilfeldig", "salg", "lonn")));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.utbetalinger).isEmpty();
//    }
//
//    @Test
//    void bostotte_skalBareHaUtRiktigSak() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithSaker(true, asList("tilfeldig", "salg", "lonn")));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.saker).hasSize(1);
//    }
//
//    @Test
//    void bostotte_skalIkkeHaSak() {
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
//                createJsonInternalSoknadWithSaker(false, asList("tilfeldig", "salg", "lonn")));
//
//        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);
//
//        assertThat(bostotteFrontend.saker).isEmpty();
//    }
//
//    @Test
//    void bostotte_skalGiSamtykke() {
//        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSaker(false, asList("tilfeldig", "salg", "lonn"));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        bostotteRessurs.updateSamtykke(BEHANDLINGSID, true, "token");
//
//        // Sjekker kaller til bostotteSystemdata
//        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
//        verify(bostotteSystemdata).updateSystemdataIn(argument.capture(), anyString());
//        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
//        assertThat(fangetBekreftelse.getType()).isEqualTo(BOSTOTTE_SAMTYKKE);
//        assertThat(fangetBekreftelse.getVerdi()).isTrue();
//
//        // Sjekker lagring av soknaden
//        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        assertThat(spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).hasSize(1);
//        JsonOkonomibekreftelse spartBekreftelse = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse().get(0);
//        assertThat(spartBekreftelse.getType()).isEqualTo(BOSTOTTE_SAMTYKKE);
//        assertThat(spartBekreftelse.getVerdi()).isTrue();
//    }
//
//    @Test
//    void bostotte_skalTaBortSamtykke() {
//        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSaker(false, asList("tilfeldig", "salg", "lonn"));
//        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
//        setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, true, "");
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
//        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
//
//        bostotteRessurs.updateSamtykke(BEHANDLINGSID, false, "token");
//
//        // Sjekker kaller til bostotteSystemdata
//        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
//        verify(bostotteSystemdata).updateSystemdataIn(argument.capture(), anyString());
//        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
//        assertThat(fangetBekreftelse.getType()).isEqualTo(BOSTOTTE_SAMTYKKE);
//        assertThat(fangetBekreftelse.getVerdi()).isFalse();
//
//        // Sjekker lagring av soknaden
//        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        JsonOkonomiopplysninger sparteOpplysninger = spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
//        assertThat(sparteOpplysninger.getBekreftelse()).hasSize(1);
//        JsonOkonomibekreftelse spartBekreftelse = sparteOpplysninger.getBekreftelse().get(0);
//        assertThat(spartBekreftelse.getType()).isEqualTo(BOSTOTTE_SAMTYKKE);
//        assertThat(spartBekreftelse.getVerdi()).isFalse();
//    }
//
//    @Test
//    void bostotte_skalIkkeForandreSamtykke() {
//        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSaker(false, asList("tilfeldig", "salg", "lonn"));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
//
//        bostotteRessurs.updateSamtykke(BEHANDLINGSID, false, "token");
//
//        // Sjekker kaller til bostotteSystemdata
//        verify(bostotteSystemdata, times(0)).updateSystemdataIn(any(), anyString());
//
//        // Sjekker lagring av soknaden
//        verify(soknadUnderArbeidRepository, times(0)).oppdaterSoknadsdata(any(), anyString());
//
//        // Sjekker soknaden
//        assertThat(soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).isEmpty();
//    }
//
//    @Test
//    void getBostotteSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> bostotteRessurs.hentBostotte(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putBostotteSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        var bostotteFrontend = new BostotteFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend, "token"));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void putSamtykkeSkalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> bostotteRessurs.updateSamtykke(BEHANDLINGSID, true, "token"));
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
//    private SoknadUnderArbeid createJsonInternalSoknadWithBostotte(Boolean verdi) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().withBekreftelse(
//                Collections.singletonList(new JsonOkonomibekreftelse()
//                        .withKilde(JsonKilde.BRUKER)
//                        .withType(BOSTOTTE)
//                        .withVerdi(verdi)));
//        return soknadUnderArbeid;
//    }
//
//    private SoknadUnderArbeid createJsonInternalSoknadWithBostotteUtbetalinger(Boolean harUtbetalinger, List<String> utbetalingTyper) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<>();
//        for (String utbetaling : utbetalingTyper) {
//            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
//                    .withKilde(JsonKilde.SYSTEM)
//                    .withType(utbetaling)
//                    .withTittel("tittel"));
//        }
//        if (harUtbetalinger) {
//            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
//                    .withKilde(JsonKilde.SYSTEM)
//                    .withType(UTBETALING_HUSBANKEN)
//                    .withTittel("tittel"));
//        }
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(utbetalinger);
//        return soknadUnderArbeid;
//    }
//
//    private SoknadUnderArbeid createJsonInternalSoknadWithSaker(Boolean harSaker, List<String> saksTyper) {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        List<JsonBostotteSak> saker = new ArrayList<>();
//        for (String sak : saksTyper) {
//            saker.add(new JsonBostotteSak()
//                    .withKilde(JsonKildeSystem.SYSTEM)
//                    .withType(sak)
//                    .withStatus("STATUS"));
//        }
//        if (harSaker) {
//            saker.add(new JsonBostotteSak()
//                    .withKilde(JsonKildeSystem.SYSTEM)
//                    .withType(UTBETALING_HUSBANKEN)
//                    .withStatus("UNDER_BEHANDLING"));
//        }
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().setSaker(saker);
//        return soknadUnderArbeid;
//    }
//}
