package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt.StudielanRessurs.StudielanFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
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
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Test(expected = AuthorizationException.class)
    public void getStudielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test(expected = AuthorizationException.class)
    public void putStudielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var studielanFrontend = new StudielanFrontend();
        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend);

        verifyNoInteractions(soknadUnderArbeidRepository);
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
