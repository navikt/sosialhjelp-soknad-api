package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.BostotteRessurs.BostotteFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte.HUSBANKEN_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BostotteRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String BEKREFTELSE_TYPE = "bostotte";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BostotteRessurs bostotteRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getBostotteSkalReturnereNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        assertThat(bostotteFrontend.bekreftelse, nullValue());
    }

    @Test
    public void getBostotteSkalReturnereBekreftetBostotte(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotte(true));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        assertTrue(bostotteFrontend.bekreftelse);
    }

    @Test
    public void getBostotteSkalReturnereHarIkkeBostotte(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotte(false));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        assertFalse(bostotteFrontend.bekreftelse);
    }

    @Test
    public void putBostotteSkalSetteBostotteOgLeggeTilInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BostotteRessurs.BostotteFrontend bostotteFrontend = new BostotteRessurs.BostotteFrontend();
        bostotteFrontend.setBekreftelse(true);
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertEquals(inntekt.get(0).getType(), BEKREFTELSE_TYPE);
        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
        assertThat(bostotte.getKilde(), is(JsonKilde.BRUKER));
        assertThat(bostotte.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(bostotte.getVerdi());
    }

    @Test
    public void putBostotteSkalSetteHarIkkeBostotteOgSletteInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        SoknadUnderArbeid soknad = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        ArrayList<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
        inntekt.add(new JsonOkonomioversiktInntekt().withType(BEKREFTELSE_TYPE));
        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        BostotteRessurs.BostotteFrontend bostotteFrontend = new BostotteRessurs.BostotteFrontend();
        bostotteFrontend.setBekreftelse(false);
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> jsonInntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertTrue(jsonInntekt.isEmpty());
        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
        assertThat(bostotte.getKilde(), is(JsonKilde.BRUKER));
        assertThat(bostotte.getType(), is(BEKREFTELSE_TYPE));
        assertFalse(bostotte.getVerdi());
    }

    @Test
    public void bostotte_skalBareHaUtRiktigUtbetaling() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotteUtbetalinger(true, asList("tilfeldig", "salg", "lonn")));

        BostotteRessurs.BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        Assertions.assertThat(bostotteFrontend.utbetalinger).hasSize(1);
    }

    @Test
    public void bostotte_skalIkkeHaUtbetaling() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotteUtbetalinger(false, asList("tilfeldig", "salg", "lonn")));

        BostotteRessurs.BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        Assertions.assertThat(bostotteFrontend.utbetalinger).hasSize(0);
    }

    @Test
    public void bostotte_skalBareHaUtRiktigSak() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSaker(true, asList("tilfeldig", "salg", "lonn")));

        BostotteRessurs.BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        Assertions.assertThat(bostotteFrontend.saker).hasSize(1);
    }

    @Test
    public void bostotte_skalIkkeHaSak() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSaker(false, asList("tilfeldig", "salg", "lonn")));

        BostotteRessurs.BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotte(BEHANDLINGSID);

        Assertions.assertThat(bostotteFrontend.saker).hasSize(0);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBostotte(Boolean verdi) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().withBekreftelse(
                Collections.singletonList(new JsonOkonomibekreftelse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(BEKREFTELSE_TYPE)
                        .withVerdi(verdi)));
        return soknadUnderArbeid;
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBostotteUtbetalinger(Boolean harUtbetalinger, List<String> utbetalingTyper) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<>();
        for (String utbetaling: utbetalingTyper) {
            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(utbetaling)
                    .withTittel("tittel"));
        }
        if(harUtbetalinger) {
            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(HUSBANKEN_TYPE)
                    .withTittel("tittel"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(utbetalinger);
        return soknadUnderArbeid;
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithSaker(Boolean harSaker, List<String> saksTyper) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomiOpplysningSak> saker = new ArrayList<>();
        for (String sak: saksTyper) {
            saker.add(new JsonOkonomiOpplysningSak()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(sak)
                    .withStatus("STATUS"));
        }
        if(harSaker) {
            saker.add(new JsonOkonomiOpplysningSak()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(HUSBANKEN_TYPE)
                    .withStatus("UNDER_BEHANDLING"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setSak(saker);
        return soknadUnderArbeid;
    }
}
