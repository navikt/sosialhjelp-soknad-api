package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs.LegacyNavEnhetFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.NavEnhetRessurs.NavEnhetFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NavEnhetRessursTest {

    private static final String BEHANDLINGSID = "123";

    private static final JsonAdresse OPPHOLDSADRESSE = new JsonGateAdresse()
            .withKilde(JsonKilde.BRUKER)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode("NOR")
            .withKommunenummer("123")
            .withAdresselinjer(null)
            .withBolignummer("1")
            .withPostnummer("2")
            .withPoststed("Oslo")
            .withGatenavn("Sanntidsgata")
            .withHusnummer("1337")
            .withHusbokstav("A");

    private static final String ENHETSNAVN = "NAV Testenhet";
    private static final String KOMMUNENAVN = "Test kommune";
    private static final String ORGNR = "123456789";
    private static final String ENHETSNAVN_2 = "NAV Van";
    private static final String KOMMUNENAVN_2 = "Enummok kommune";
    private static final String ORGNR_2 = "123454321";
    private static final JsonSoknadsmottaker SOKNADSMOTTAKER = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN + ", " + KOMMUNENAVN)
            .withOrganisasjonsnummer(ORGNR);

    private static final JsonSoknadsmottaker SOKNADSMOTTAKER_2 = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN_2 + ", " + KOMMUNENAVN_2)
            .withOrganisasjonsnummer(ORGNR_2);

    private static final LegacyNavEnhetFrontend LEGACY_SOKNADSMOTTAKER = new LegacyNavEnhetFrontend()
            .withEnhetsnavn(ENHETSNAVN)
            .withKommunenavn(KOMMUNENAVN)
            .withSosialOrgnr(ORGNR);

    private static final LegacyNavEnhetFrontend LEGACY_SOKNADSMOTTAKER_2 = new LegacyNavEnhetFrontend()
            .withEnhetsnavn(ENHETSNAVN_2)
            .withKommunenavn(KOMMUNENAVN_2)
            .withSosialOrgnr(ORGNR_2);

    private static final String EIER = "123456789101";

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
    private SoknadsmottakerRessurs soknadsmottakerRessurs;

    @InjectMocks
    private NavEnhetRessurs navEnhetRessurs;

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
    public void getNavEnheterSkalReturnereEnheterRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg.FOLKEREGISTRERT, SOKNADSMOTTAKER));
        when(soknadsmottakerRessurs.findSoknadsmottaker(BEHANDLINGSID, "folkeregistrert")).thenReturn(
                Arrays.asList(LEGACY_SOKNADSMOTTAKER, LEGACY_SOKNADSMOTTAKER_2));

        final List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertThatEnheterAreCorrectlyConverted(navEnhetFrontends, Arrays.asList(SOKNADSMOTTAKER, SOKNADSMOTTAKER_2));
        assertThat(navEnhetFrontends.get(0).valgt, is(true));
        assertThat(navEnhetFrontends.get(1).valgt, is(false));
    }

    @Test
    public void getNavEnheterSkalReturnereTomListeNaarOppholdsadresseIkkeErValgt(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(null, null));
        when(soknadsmottakerRessurs.findSoknadsmottaker(BEHANDLINGSID, null)).thenReturn(Collections.emptyList());

        final List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertTrue(navEnhetFrontends.isEmpty());
    }

    @Test
    public void putNavEnhetSkalSetteNavenhet(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg.FOLKEREGISTRERT, SOKNADSMOTTAKER)));

        ignoreTilgangskontrollAndLegacyUpdate();
        final NavEnhetFrontend navEnhetFrontend = new NavEnhetFrontend()
                .withEnhetsnavn(ENHETSNAVN_2)
                .withKommunenavn(KOMMUNENAVN_2)
                .withOrgnr(ORGNR_2);
        navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonSoknadsmottaker jsonSoknadsmottaker = soknadUnderArbeid.getJsonInternalSoknad().getMottaker();
        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, jsonSoknadsmottaker);
    }

    private void assertThatEnheterAreCorrectlyConverted(List<NavEnhetFrontend> navEnhetFrontends, List<JsonSoknadsmottaker> jsonSoknadsmottakers){
        for (int i = 0; i < navEnhetFrontends.size(); i++){
            assertThatEnhetIsCorrectlyConverted(navEnhetFrontends.get(i), jsonSoknadsmottakers.get(i));
        }
    }

    private void assertThatEnhetIsCorrectlyConverted(NavEnhetFrontend navEnhetFrontend, JsonSoknadsmottaker soknadsmottaker) {
        if (navEnhetFrontend == null){
            assertThat(soknadsmottaker, nullValue());
            return;
        }

        final String kombinertnavn = soknadsmottaker.getNavEnhetsnavn();
        final String enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','));
        final String kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2);

        assertThat("Enhetsnavn", navEnhetFrontend.enhetsnavn, is(enhetsnavn));
        assertThat("kommunenavn", navEnhetFrontend.kommunenavn, is(kommunenavn));
        assertThat("orgnr", navEnhetFrontend.orgnr, is(soknadsmottaker.getOrganisasjonsnummer()));
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

    private SoknadUnderArbeid createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg valg, JsonSoknadsmottaker soknadsmottaker) {
        return new SoknadUnderArbeid()
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withSoknad(new JsonSoknad()
                                .withData(new JsonData()
                                        .withPersonalia(new JsonPersonalia()
                                                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(valg))
                                                .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                        .withVerdi(EIER)
                                                )
                                        )
                                )
                        ).withMottaker(soknadsmottaker)
                );
    }
}