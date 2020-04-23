package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.personalia.NavEnhetRessurs.NavEnhetFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    private static final String KOMMUNENR = KommuneTilNavEnhetMapper.getDigisoskommuner().get(0);
    private static final String ENHETSNR = "1234";
    private static final String ORGNR = "123456789";
    private static final String ENHETSNAVN_2 = "NAV Van";
    private static final String KOMMUNENAVN_2 = "Enummok kommune";
    private static final String KOMMUNENR_2 = KommuneTilNavEnhetMapper.getDigisoskommuner().get(1);
    private static final String ENHETSNR_2 = "5678";
    private static final String ORGNR_2 = "987654321";
    private static final JsonSoknadsmottaker SOKNADSMOTTAKER = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN + ", " + KOMMUNENAVN)
            .withEnhetsnummer(ENHETSNR)
            .withKommunenummer(KOMMUNENR);


    private static final JsonSoknadsmottaker SOKNADSMOTTAKER_2 = new JsonSoknadsmottaker()
            .withNavEnhetsnavn(ENHETSNAVN_2 + ", " + KOMMUNENAVN_2)
            .withEnhetsnummer(ENHETSNR_2)
            .withKommunenummer(KOMMUNENR_2);

    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG = new AdresseForslag();
    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG_2 = new AdresseForslag();

    private static final NavEnhet NAV_ENHET = new NavEnhet();
    private static final NavEnhet NAV_ENHET_2 = new NavEnhet();

    static {
        SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning = ENHETSNAVN;
        SOKNADSMOTTAKER_FORSLAG.kommunenavn = KOMMUNENAVN;
        SOKNADSMOTTAKER_FORSLAG.kommunenummer = KOMMUNENR;

        NAV_ENHET.navn = ENHETSNAVN;
        NAV_ENHET.sosialOrgnr = ORGNR;
        NAV_ENHET.enhetNr = ENHETSNR;

        SOKNADSMOTTAKER_FORSLAG_2.geografiskTilknytning = ENHETSNAVN_2;
        SOKNADSMOTTAKER_FORSLAG_2.kommunenavn = KOMMUNENAVN_2;
        SOKNADSMOTTAKER_FORSLAG_2.kommunenummer = KOMMUNENR_2;

        NAV_ENHET_2.navn = ENHETSNAVN_2;
        NAV_ENHET_2.sosialOrgnr = ORGNR_2;
        NAV_ENHET_2.enhetNr = ENHETSNR_2;
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

    @Mock
    private KommuneInfoService kommuneInfoService;

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
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(soknadsmottakerService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq("folkeregistrert"))).thenReturn(
                Arrays.asList(SOKNADSMOTTAKER_FORSLAG, SOKNADSMOTTAKER_FORSLAG_2));
        when(norgService.getEnhetForGt(ENHETSNAVN)).thenReturn(NAV_ENHET);
        when(norgService.getEnhetForGt(ENHETSNAVN_2)).thenReturn(NAV_ENHET_2);

        List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertThatEnheterAreCorrectlyConverted(navEnhetFrontends, Arrays.asList(SOKNADSMOTTAKER, SOKNADSMOTTAKER_2));
        assertThat(navEnhetFrontends.get(0).valgt, is(true));
        assertThat(navEnhetFrontends.get(1).valgt, is(false));
    }

    @Test
    public void getValgtNavEnhetSkalReturnereEnhetRiktigKonvertert(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        NavEnhetFrontend navEnhetFrontend = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID);

        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, SOKNADSMOTTAKER);
        assertThat(navEnhetFrontend.valgt, is(true));
    }

    @Test
    public void getNavEnheterSkalReturnereTomListeNaarOppholdsadresseIkkeErValgt(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(soknadsmottakerService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq(null))).thenReturn(new ArrayList<>());
        when(norgService.getEnhetForGt(ENHETSNAVN)).thenReturn(NAV_ENHET);
        when(norgService.getEnhetForGt(ENHETSNAVN_2)).thenReturn(NAV_ENHET_2);

        List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);

        assertTrue(navEnhetFrontends.isEmpty());
    }

    @Test
    public void getValgtNavEnhetSkalReturnereNullNarOppholdsadresseIkkeErValgt(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        NavEnhetFrontend navEnhetFrontends = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID);

        assertNull(navEnhetFrontends);
    }

    @Test
    public void putNavEnhetSkalSetteNavenhet(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        NavEnhetFrontend navEnhetFrontend = new NavEnhetFrontend()
                .withEnhetsnavn(ENHETSNAVN_2)
                .withKommunenavn(KOMMUNENAVN_2)
                .withOrgnr(ORGNR_2)
                .withEnhetsnr(ENHETSNR_2);

        navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend);

        SoknadUnderArbeid updatedSoknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonSoknadsmottaker jsonSoknadsmottaker = updatedSoknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker();
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

        String kombinertnavn = soknadsmottaker.getNavEnhetsnavn();
        String enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','));
        String kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2);

        assertThat("Enhetsnavn", navEnhetFrontend.enhetsnavn, is(enhetsnavn));
        assertThat("kommunenavn", navEnhetFrontend.kommunenavn, is(kommunenavn));
        assertThat("enhetsnr", navEnhetFrontend.enhetsnr, is(soknadsmottaker.getEnhetsnummer()));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }
}