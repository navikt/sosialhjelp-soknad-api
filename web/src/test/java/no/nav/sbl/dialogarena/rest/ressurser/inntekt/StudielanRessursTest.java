package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.StudielanRessurs.StudielanFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StudielanRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private StudielanRessurs studielanRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
    }

    @Test
    public void getStudielanSkalReturnereNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, null));

        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        assertThat(studielanFrontend.skalVises, is(true));
        assertThat(studielanFrontend.bekreftelse, nullValue());
    }

    @Test
    public void getStudielanSkalReturnereBekreftetStudielan(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, true));

        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        assertThat(studielanFrontend.skalVises, is(true));
        assertTrue(studielanFrontend.bekreftelse);
    }

    @Test
    public void getStudielanSkalReturnereHarIkkeStudielan(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, false));

        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        assertThat(studielanFrontend.skalVises, is(true));
        assertFalse(studielanFrontend.bekreftelse);
    }

    @Test
    public void getStudielanSkalReturnereSkalIkkeVisesHvisIkkeStudent(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithErStudentStudielanBekreftelse(false, null));

        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        assertThat(studielanFrontend.skalVises, is(false));
        assertThat(studielanFrontend.bekreftelse, nullValue());
    }

    @Test
    public void getStudielanSkalReturnereSkalIkkeVisesHvisStudentSporsmalIkkeBesvart(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithErStudentStudielanBekreftelse(null, null));

        StudielanFrontend studielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        assertThat(studielanFrontend.skalVises, is(false));
        assertThat(studielanFrontend.bekreftelse, nullValue());
    }

    @Test
    public void putStudielanSkalSetteStudielanOgLeggeTilInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        StudielanFrontend studielanFrontend = new StudielanFrontend();
        studielanFrontend.setBekreftelse(true);
        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertEquals(inntekt.get(0).getType(), STUDIELAN);
        JsonOkonomibekreftelse studielan = bekreftelser.get(0);
        assertThat(studielan.getKilde(), is(JsonKilde.BRUKER));
        assertThat(studielan.getType(), is(STUDIELAN));
        assertTrue(studielan.getVerdi());
    }

    @Test
    public void putStudielanSkalSetteHarIkkeStudielanOgSletteInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        SoknadUnderArbeid soknad = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        ArrayList<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
        inntekt.add(new JsonOkonomioversiktInntekt().withType(STUDIELAN));
        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        StudielanFrontend studielanFrontend = new StudielanFrontend();
        studielanFrontend.setBekreftelse(false);
        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> jsonInntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertTrue(jsonInntekt.isEmpty());
        JsonOkonomibekreftelse studielan = bekreftelser.get(0);
        assertThat(studielan.getKilde(), is(JsonKilde.BRUKER));
        assertThat(studielan.getType(), is(STUDIELAN));
        assertFalse(studielan.getVerdi());
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithErStudentStudielanBekreftelse(Boolean erStudent, Boolean verdi) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().withBekreftelse(
                asList(new JsonOkonomibekreftelse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(STUDIELAN)
                        .withVerdi(verdi)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning().setErStudent(erStudent);
        return soknadUnderArbeid;
    }
}
