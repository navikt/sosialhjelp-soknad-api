package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.TelefonnummerRessurs.TelefonnummerFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.TelefonnummerSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TelefonnummerRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String TELEFONNUMMER_BRUKER = "98765432";
    private static final String TELEFONNUMMER_SYSTEM = "23456789";
    private static final String TELEFONNUMMER_SYSTEM_OPPDATERT = "12345678";

    @Mock
    private LegacyHelper legacyHelper;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private TelefonnummerSystemdata telefonnummerSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @InjectMocks
    private TelefonnummerRessurs telefonnummerRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getTelefonnummerSkalReturnereSystemTelefonnummer(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(JsonKilde.SYSTEM, TELEFONNUMMER_SYSTEM));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM);

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(telefonnummerFrontend.systemverdi, is(TELEFONNUMMER_SYSTEM));
        assertThat(telefonnummerFrontend.brukerdefinert, is(false));
    }

    @Test
    public void getTelefonnummerSkalReturnereOppdatertSystemTelefonnummerFraTPS(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(JsonKilde.SYSTEM, TELEFONNUMMER_SYSTEM));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(TELEFONNUMMER_SYSTEM_OPPDATERT);

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(telefonnummerFrontend.systemverdi, is(TELEFONNUMMER_SYSTEM_OPPDATERT));
        assertThat(telefonnummerFrontend.brukerdefinert, is(false));
    }

    @Test
    public void getTelefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithTelefonnummer(null, null));
        when(telefonnummerSystemdata.innhentSystemverdiTelefonnummer(anyString())).thenReturn(null);

        final TelefonnummerFrontend telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID);

        assertThat(telefonnummerFrontend.brukerutfyltVerdi, nullValue());
        assertThat(telefonnummerFrontend.systemverdi, nullValue());
        assertThat(telefonnummerFrontend.brukerdefinert, is(true));
    }

    @Test
    public void getTelefonnummerSkalReturnereBrukerutfyltTelefonnummer(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
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
                Optional.of(createJsonInternalSoknadWithTelefonnummer(null, null)));
        ignoreTilgangskontrollAndLegacyUpdate();

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
                Optional.of(createJsonInternalSoknadWithTelefonnummer(null, null)));
        ignoreTilgangskontrollAndLegacyUpdate();

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
                Optional.of(createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER)));
        ignoreTilgangskontrollAndLegacyUpdate();

        final TelefonnummerFrontend telefonnummerFrontend = new TelefonnummerFrontend()
                .withBrukerdefinert(false)
                .withSystemverdi(TELEFONNUMMER_SYSTEM);
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonTelefonnummer telefonnummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getTelefonnummer();
        assertThat(telefonnummer.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(telefonnummer.getVerdi(), is(TELEFONNUMMER_SYSTEM));
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

    private SoknadUnderArbeid createJsonInternalSoknadWithTelefonnummer(JsonKilde kilde, String verdi) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withPersonalia(new JsonPersonalia()
                                                .withTelefonnummer(verdi == null ? null : new JsonTelefonnummer()
                                                        .withKilde(kilde)
                                                        .withVerdi(verdi)
                                                )
                                                .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                        .withVerdi(EIER)
                                                )
                                        )
                                )
                        )
                );
    }
}
