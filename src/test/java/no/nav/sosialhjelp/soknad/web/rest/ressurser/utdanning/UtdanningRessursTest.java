package no.nav.sosialhjelp.soknad.web.rest.ressurser.utdanning;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.utdanning.UtdanningRessurs.UtdanningFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtdanningRessursTest {

    private static final String EIER = "123456789101";
    private static final String BEHANDLINGSID = "123";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private UtdanningRessurs utdanningRessurs;

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningUtenErStudentOgStudentgrad(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(null, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent).isNull();
        assertThat(utdanningFrontend.studengradErHeltid).isNull();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErIkkeStudent(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.FALSE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent).isEqualTo(Boolean.FALSE);
        assertThat(utdanningFrontend.studengradErHeltid).isNull();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudent(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent).isEqualTo(Boolean.TRUE);
        assertThat(utdanningFrontend.studengradErHeltid).isNull();
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradHeltid(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.HELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent).isEqualTo(Boolean.TRUE);
        assertThat(utdanningFrontend.studengradErHeltid).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradDeltid(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.DELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent).isEqualTo(Boolean.TRUE);
        assertThat(utdanningFrontend.studengradErHeltid).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudent(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(null, null));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withErStudent(Boolean.TRUE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utdanning.getErStudent()).isEqualTo(Boolean.TRUE);
        assertThat(utdanning.getStudentgrad()).isNull();
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudentOgStudentgrad(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(null, null));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withErStudent(Boolean.TRUE)
                .withStudengradErHeltid(Boolean.TRUE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utdanning.getErStudent()).isEqualTo(Boolean.TRUE);
        assertThat(utdanning.getStudentgrad()).isEqualTo(JsonUtdanning.Studentgrad.HELTID);
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErIkkeStudentOgSletteStudentgrad(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(true, JsonUtdanning.Studentgrad.DELTID));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withErStudent(Boolean.FALSE)
                .withStudengradErHeltid(Boolean.FALSE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utdanning.getErStudent()).isEqualTo(Boolean.FALSE);
        assertThat(utdanning.getStudentgrad()).isNull();
    }

    @Test(expected = AuthorizationException.class)
    public void getUtdanningSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test(expected = AuthorizationException.class)
    public void putUtdanningSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        var utdanningFrontend = new UtdanningFrontend();
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithUtdanning(Boolean erStudent, JsonUtdanning.Studentgrad studentgrad) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning()
                .withKilde(JsonKilde.BRUKER)
                .withErStudent(erStudent)
                .withStudentgrad(studentgrad);
        return soknadUnderArbeid;
    }
}
