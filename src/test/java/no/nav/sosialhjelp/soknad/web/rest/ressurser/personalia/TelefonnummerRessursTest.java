package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.TelefonnummerRessurs.TelefonnummerFrontend;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TelefonnummerRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String TELEFONNUMMER_BRUKER = "98765432";
    private static final String TELEFONNUMMER_SYSTEM = "23456789";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private TelefonnummerSystemdata telefonnummerSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private TelefonnummerRessurs telefonnummerRessurs;

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        doCallRealMethod().when(telefonnummerSystemdata).updateSystemdataIn(any(SoknadUnderArbeid.class), any());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void getTelefonnummerSkalReturnereSystemTelefonnummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(JsonKilde.SYSTEM, TELEFONNUMMER_SYSTEM));

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(telefonnummerFrontend.systemverdi, is(TELEFONNUMMER_SYSTEM));
        assertThat(telefonnummerFrontend.brukerdefinert, is(false));
    }

    @Test
    public void getTelefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(null, null));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(null);

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(telefonnummerFrontend.systemverdi, nullValue());
        assertThat(telefonnummerFrontend.brukerdefinert, is(true));
    }

    @Test
    public void getTelefonnummerSkalReturnereBrukerutfyltTelefonnummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, is(TELEFONNUMMER_BRUKER));
        assertThat(telefonnummerFrontend.systemverdi, is(TELEFONNUMMER_SYSTEM));
        assertThat(telefonnummerFrontend.brukerdefinert, is(true));
    }

    @Test
    public void putTelefonnummerSkalLageNyJsonTelefonnummerDersomDenVarNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(null, null));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
                .withBrukerdefinert(true)
                .withBrukerutfyltVerdi(TELEFONNUMMER_BRUKER);
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
        assertThat(telefonnummer.getKilde(), is(JsonKilde.BRUKER));
        assertThat(telefonnummer.getVerdi(), is(TELEFONNUMMER_BRUKER));
    }

    @Test
    public void putTelefonnummerSkalOppdatereBrukerutfyltTelefonnummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(null, null));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
                .withBrukerdefinert(true)
                .withBrukerutfyltVerdi(TELEFONNUMMER_BRUKER);
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
        assertThat(telefonnummer.getKilde(), is(JsonKilde.BRUKER));
        assertThat(telefonnummer.getVerdi(), is(TELEFONNUMMER_BRUKER));
    }

    @Test
    public void putTelefonnummerSkalOverskriveBrukerutfyltTelefonnummerMedSystemTelefonnummer(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
                .withBrukerdefinert(false);
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
        assertThat(telefonnummer.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(telefonnummer.getVerdi(), is(TELEFONNUMMER_SYSTEM));
    }

    @Test(expected = AuthorizationException.class)
    public void getTelefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test(expected = AuthorizationException.class)
    public void putTelefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        var telefonnummerFrontend = new TelefonnummerFrontend();
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithTelefonnummer(JsonKilde kilde, String verdi) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withTelefonnummer(verdi == null ? null : new JsonTelefonnummer()
                        .withKilde(kilde)
                        .withVerdi(verdi));
        return soknadUnderArbeid;
    }
}
