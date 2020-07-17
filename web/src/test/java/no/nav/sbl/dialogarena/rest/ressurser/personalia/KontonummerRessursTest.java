package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.personalia.KontonummerRessurs.KontonummerFrontend;

import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.KontonummerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class KontonummerRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String KONTONUMMER_BRUKER = "11122233344";
    private static final String KONTONUMMER_SYSTEM = "44333222111";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private KontonummerSystemdata kontonummerSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private KontonummerRessurs kontonummerRessurs;

    @Before
    public void setUp() {
        //SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        doCallRealMethod().when(kontonummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class), any());
    }

    @After
    public void tearDown() {
        // SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getKontonummerSkalReturnereSystemKontonummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, KONTONUMMER_SYSTEM));
        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);

        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);

        assertThat(kontonummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(kontonummerFrontend.systemverdi, is(KONTONUMMER_SYSTEM));
        assertThat(kontonummerFrontend.harIkkeKonto, nullValue());
        assertThat(kontonummerFrontend.brukerdefinert, is(false));
    }

    @Test
    public void getKontonummerSkalReturnereBrukerutfyltKontonummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER));
        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);

        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);

        assertThat(kontonummerFrontend.brukerutfyltVerdi, is(KONTONUMMER_BRUKER));
        assertThat(kontonummerFrontend.systemverdi, is(KONTONUMMER_SYSTEM));
        assertThat(kontonummerFrontend.harIkkeKonto, nullValue());
        assertThat(kontonummerFrontend.brukerdefinert, is(true));
    }

    @Test
    public void getKontonummerSkalReturnereKontonummerLikNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, null));
        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(null);

        final KontonummerFrontend kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID);

        assertThat(kontonummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(kontonummerFrontend.systemverdi, nullValue());
        assertThat(kontonummerFrontend.harIkkeKonto, nullValue());
        assertThat(kontonummerFrontend.brukerdefinert, is(true));
    }

    @Test
    public void putKontonummerSkalSetteBrukerutfyltKontonummer(){
        startWithEmptyKontonummerAndNoSystemKontonummer();
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        final KontonummerFrontend kontonummerFrontend = new KontonummerFrontend()
                .withBrukerdefinert(true)
                .withBrukerutfyltVerdi(KONTONUMMER_BRUKER);
        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
        assertThat(kontonummer.getKilde(), is(JsonKilde.BRUKER));
        assertThat(kontonummer.getHarIkkeKonto(), nullValue());
        assertThat(kontonummer.getVerdi(), is(KONTONUMMER_BRUKER));
    }

    @Test
    public void putKontonummerSkalOverskriveBrukerutfyltKontonummerMedSystemKontonummer(){
        startWithBrukerKontonummerAndSystemKontonummerInTPS();
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        final KontonummerFrontend kontonummerFrontend = new KontonummerFrontend()
                .withBrukerdefinert(false)
                .withSystemverdi(KONTONUMMER_SYSTEM);
        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonKontonummer kontonummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer();
        assertThat(kontonummer.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(kontonummer.getHarIkkeKonto(), nullValue());
        assertThat(kontonummer.getVerdi(), is(KONTONUMMER_SYSTEM));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private void startWithBrukerKontonummerAndSystemKontonummerInTPS() {
        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(KONTONUMMER_SYSTEM);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER));
    }

    private void startWithEmptyKontonummerAndNoSystemKontonummer() {
        when(kontonummerSystemdata.innhentSystemverdiKontonummer(anyString())).thenReturn(null);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, null));
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithKontonummer(JsonKilde kilde, String verdi) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getKontonummer()
                .withKilde(kilde)
                .withVerdi(verdi);
        return  soknadUnderArbeid;
    }

}
