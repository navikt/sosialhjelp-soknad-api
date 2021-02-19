package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt.UtbetalingRessurs.UtbetalingerFrontend;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private UtbetalingRessurs utbetalingRessurs;

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void getUtbetalingerSkalReturnereBekreftelseLikNullOgAltFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertThat(utbetalingerFrontend.bekreftelse, nullValue());
        assertFalse(utbetalingerFrontend.forsikring);
        assertFalse(utbetalingerFrontend.salg);
        assertFalse(utbetalingerFrontend.utbytte);
        assertFalse(utbetalingerFrontend.annet);
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getUtbetalingerSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING,
                        UTBETALING_ANNET), null));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertTrue(utbetalingerFrontend.bekreftelse);
        assertTrue(utbetalingerFrontend.utbytte);
        assertTrue(utbetalingerFrontend.salg);
        assertTrue(utbetalingerFrontend.forsikring);
        assertTrue(utbetalingerFrontend.annet);
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getUtbetalingerSkalReturnereBeskrivelseAvAnnet(){
        String beskrivelse = "Lottogevinst";
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_ANNET), beskrivelse));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertTrue(utbetalingerFrontend.bekreftelse);
        assertTrue(utbetalingerFrontend.annet);
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet, is(beskrivelse));
    }

    @Test
    public void putUtbetalingerSkalSetteAltFalseDersomManVelgerHarIkkeUtbetalinger(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING,
                        UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertFalse(utbetalingBekreftelse.getVerdi());
        assertTrue(utbetalinger.isEmpty());
    }

    @Test
    public void putUtbetalingerSkalSetteAlleBekreftelserLikFalse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG,
                        UTBETALING_FORSIKRING, UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling();
        assertFalse(utbetalingBekreftelse.getVerdi());
        assertTrue(utbetalinger.isEmpty());
        assertThat(beskrivelse, is(""));
    }

    @Test
    public void putUtbetalingerSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(true);
        utbetalingerFrontend.setForsikring(true);
        utbetalingerFrontend.setSalg(true);
        utbetalingerFrontend.setUtbytte(false);
        utbetalingerFrontend.setAnnet(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalingBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetalingBekreftelse.getType(), is(BEKREFTELSE_UTBETALING));
        assertTrue(utbetalingBekreftelse.getVerdi());
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_FORSIKRING)));
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_SALG)));
        assertFalse(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_UTBYTTE)));
        assertFalse(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_ANNET)));
    }

    @Test
    public void putUtbetalingerSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(true);
        utbetalingerFrontend.setForsikring(true);
        utbetalingerFrontend.setSalg(true);
        utbetalingerFrontend.setUtbytte(true);
        utbetalingerFrontend.setAnnet(true);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalingBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetalingBekreftelse.getType(), is(BEKREFTELSE_UTBETALING));
        assertTrue(utbetalingBekreftelse.getVerdi());
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_FORSIKRING)));
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_SALG)));
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_UTBYTTE)));
        assertTrue(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_ANNET)));
    }

    @Test
    public void putUtbetalingerSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling();
        assertFalse(utbetalingBekreftelse.getVerdi());
        assertThat(beskrivelse, is(""));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithUtbetalinger(Boolean harUtbetalinger, List<String> utbetalingTyper, String beskrivelseAvAnnet) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<>();
        for (String utbetaling: utbetalingTyper) {
            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(utbetaling)
                    .withTittel("tittel"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_UTBETALING)
                .withVerdi(harUtbetalinger)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(utbetalinger);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet().withUtbetaling(beskrivelseAvAnnet));
        return soknadUnderArbeid;
    }
}
