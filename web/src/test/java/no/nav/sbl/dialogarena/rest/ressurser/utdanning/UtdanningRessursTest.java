package no.nav.sbl.dialogarena.rest.ressurser.utdanning;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.utdanning.UtdanningRessurs.UtdanningFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtdanningRessursTest {

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

    @InjectMocks
    private UtdanningRessurs utdanningRessurs;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
    }

    @Test
    public void getUtdanningSkalReturnereBrukerutfyltUtdanningUtenErStudentOgStudentgrad(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithoutErStudentAndStudentgrad());

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        assertThat(utdanningFrontend.brukerdefinert, is(true));
        assertThat(utdanningFrontend.erStudent, nullValue());
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereBrukerutfyltUtdanningMedErIkkeStudent(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.FALSE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        assertThat(utdanningFrontend.brukerdefinert, is(true));
        assertThat(utdanningFrontend.erStudent, is(Boolean.FALSE));
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereBrukerutfyltUtdanningMedErStudent(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, null));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        assertThat(utdanningFrontend.brukerdefinert, is(true));
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, nullValue());
    }

    @Test
    public void getUtdanningSkalReturnereBrukerutfyltUtdanningMedErStudentOgStudentgradHeltid(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.HELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        assertThat(utdanningFrontend.brukerdefinert, is(true));
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, is(Boolean.TRUE));
    }

    @Test
    public void getUtdanningSkalReturnereBrukerutfyltUtdanningMedErStudentOgStudentgradDeltid(){
        when(legacyHelper.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtdanning(Boolean.TRUE, JsonUtdanning.Studentgrad.DELTID));

        final UtdanningFrontend utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID);

        assertThat(utdanningFrontend.brukerdefinert, is(true));
        assertThat(utdanningFrontend.erStudent, is(Boolean.TRUE));
        assertThat(utdanningFrontend.studengradErHeltid, is(Boolean.FALSE));
    }

    @Test
    public void putUtdanningSkalSetteBrukerutfyltUtdanningMedErStudent(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithoutErStudentAndStudentgrad()));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withBrukerdefinert(true)
                .withErStudent(Boolean.TRUE);
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonUtdanning utdanning = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getUtdanning();
        assertThat(utdanning.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utdanning.getErStudent(), is(Boolean.TRUE));
        assertThat(utdanning.getStudentgrad(), nullValue());
    }

    @Test
    public void putUtdanningSkalSetteBrukerutfyltUtdanningMedErStudentOgStudentgrad(){
        ignoreTilgangskontrollAndLegacyUpdate();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithoutErStudentAndStudentgrad()));

        final UtdanningFrontend utdanningFrontend = new UtdanningFrontend()
                .withBrukerdefinert(true)
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
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withUtdanning(new JsonUtdanning()
                                                .withKilde(JsonKilde.BRUKER)
                                                .withErStudent(erStudent)
                                                .withStudentgrad(studentgrad))
                                )
                        )
                );
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithoutErStudentAndStudentgrad() {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withUtdanning(new JsonUtdanning()
                                                .withKilde(JsonKilde.BRUKER))
                                )
                        )
                );
    }
}