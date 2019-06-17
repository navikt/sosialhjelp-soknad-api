package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.personalia.NavEnhetRessurs.NavEnhetFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
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
    private static final String ORGNR = KommuneTilNavEnhetMapper.getDigisoskommuner().get(0);
    private static final String ENHETSNAVN_2 = "NAV Van";
    private static final String KOMMUNENAVN_2 = "Enummok kommune";
    private static final String ORGNR_2 = KommuneTilNavEnhetMapper.getDigisoskommuner().get(1);
    private static final JsonSoknadsmottaker SOKNADSMOTTAKER = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN + ", " + KOMMUNENAVN)
            .withOrganisasjonsnummer(ORGNR);

    private static final JsonSoknadsmottaker SOKNADSMOTTAKER_2 = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN_2 + ", " + KOMMUNENAVN_2)
            .withOrganisasjonsnummer(ORGNR_2);

    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG = new AdresseForslag();
    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG_2 = new AdresseForslag();

    private static final NavEnhet NAV_ENHET = new NavEnhet();
    private static final NavEnhet NAV_ENHET_2 = new NavEnhet();

    static {
        SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning = ENHETSNAVN;
        SOKNADSMOTTAKER_FORSLAG.kommunenavn = KOMMUNENAVN;
        SOKNADSMOTTAKER_FORSLAG.kommunenummer = ORGNR;

        NAV_ENHET.navn = ENHETSNAVN;
        NAV_ENHET.sosialOrgnr = ORGNR;

        SOKNADSMOTTAKER_FORSLAG_2.geografiskTilknytning = ENHETSNAVN_2;
        SOKNADSMOTTAKER_FORSLAG_2.kommunenavn = KOMMUNENAVN_2;
        SOKNADSMOTTAKER_FORSLAG_2.kommunenummer = ORGNR_2;

        NAV_ENHET_2.navn = ENHETSNAVN_2;
        NAV_ENHET_2.sosialOrgnr = ORGNR_2;
    }

    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadsmottakerService soknadsmottakerService;

    @Mock
    private NorgService norgService;

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
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg.FOLKEREGISTRERT, SOKNADSMOTTAKER));
        when(soknadsmottakerService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq("folkeregistrert"))).thenReturn(
                Arrays.asList(SOKNADSMOTTAKER_FORSLAG, SOKNADSMOTTAKER_FORSLAG_2));
        when(norgService.finnEnhetForGt(ENHETSNAVN)).thenReturn(NAV_ENHET);
        when(norgService.finnEnhetForGt(ENHETSNAVN_2)).thenReturn(NAV_ENHET_2);

        final List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertThatEnheterAreCorrectlyConverted(navEnhetFrontends, Arrays.asList(SOKNADSMOTTAKER, SOKNADSMOTTAKER_2));
        assertThat(navEnhetFrontends.get(0).valgt, is(true));
        assertThat(navEnhetFrontends.get(1).valgt, is(false));
    }

    @Test
    public void getNavEnheterSkalReturnereTomListeNaarOppholdsadresseIkkeErValgt(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(null, null));
        when(soknadsmottakerService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq(null))).thenReturn(new ArrayList<>());
        when(norgService.finnEnhetForGt(ENHETSNAVN)).thenReturn(NAV_ENHET);
        when(norgService.finnEnhetForGt(ENHETSNAVN_2)).thenReturn(NAV_ENHET_2);

        final List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertTrue(navEnhetFrontends.isEmpty());
    }

    @Test
    public void putNavEnhetSkalSetteNavenhet(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg.FOLKEREGISTRERT, SOKNADSMOTTAKER));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
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

    private SoknadUnderArbeid createJsonInternalSoknadWithAdresseValgAndSoknadsmottaker(JsonAdresseValg valg, JsonSoknadsmottaker soknadsmottaker) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().withMottaker(soknadsmottaker).getSoknad().getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(valg));
        return soknadUnderArbeid;
    }
}