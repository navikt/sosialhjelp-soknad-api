package no.nav.sbl.dialogarena.rest.ressurser.utdanning;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.utdanning.UtdanningRessurs.UtdanningFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
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

import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtdanningRessursTest {

    private static final String EIER = "123456789101";
    private static final String BEHANDLINGSID = "123";

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private TextService textService;

    @InjectMocks
    private UtdanningRessurs utdanningRessurs;

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
    public void getUtdanningSkalReturnereUtdanningUtenErStudentOgStudentgrad(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithUtdanning(null, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent, nullValue());
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErIkkeStudent(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.FALSE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent, is(Boolean.FALSE));
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudent(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradHeltid(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.HELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, is(Boolean.TRUE));
    }

    @Test
    public void getUtdanningSkalReturnereUtdanningMedErStudentOgStudentgradDeltid(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.DELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);
        
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, is(Boolean.FALSE));
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudent(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithUtdanning(null, null)));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withErStudent(Boolean.TRUE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utdanning.getErStudent(), is(Boolean.TRUE));
        assertThat(utdanning.getStudentgrad(), nullValue());
    }

    @Test
    public void putUtdanningSkalSetteUtdanningMedErStudentOgStudentgrad(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithUtdanning(null, null)));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withErStudent(Boolean.TRUE)
                .withStudengradErHeltid(Boolean.TRUE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utdanning.getErStudent(), is(Boolean.TRUE));
        assertThat(utdanning.getStudentgrad(), is(JsonUtdanning.Studentgrad.HELTID));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
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
