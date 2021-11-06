//package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
//import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
//import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseSokService;
//import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
//import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
//import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService;
//import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.GeografiskTilknytningService;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
//import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService;
//import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.NavEnhetRessurs.NavEnhetFrontend;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static java.util.Collections.singletonList;
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService.BYDEL_MARKA;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.anyString;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class NavEnhetRessursTest {
//
//    private static final String BEHANDLINGSID = "123";
//
//
//    private static final String OPPHOLDSADRESSE_KOMMUNENR = "0123";
//    private static final String OPPHOLDSADRESSE_BYDELSNR = "012301";
//    private static final JsonAdresse OPPHOLDSADRESSE = new JsonGateAdresse()
//            .withKilde(JsonKilde.BRUKER)
//            .withType(JsonAdresse.Type.GATEADRESSE)
//            .withLandkode("NOR")
//            .withKommunenummer(OPPHOLDSADRESSE_KOMMUNENR)
//            .withAdresselinjer(null)
//            .withBolignummer("1")
//            .withPostnummer("2")
//            .withPoststed("Oslo")
//            .withGatenavn("Sanntidsgata")
//            .withHusnummer("1337")
//            .withHusbokstav("A");
//
//    private static final String ENHETSNAVN = "NAV Testenhet";
//    private static final String KOMMUNENAVN = "Test kommune";
//    private static final String KOMMUNENR = KommuneTilNavEnhetMapper.getDigisoskommuner().get(0);
//    private static final String ENHETSNR = "1234";
//    private static final String ORGNR = "123456789";
//    private static final String ENHETSNAVN_2 = "NAV Van";
//    private static final String KOMMUNENAVN_2 = "Enummok kommune";
//    private static final String KOMMUNENR_2 = KommuneTilNavEnhetMapper.getDigisoskommuner().get(1);
//    private static final String ENHETSNR_2 = "5678";
//    private static final String ORGNR_2 = "987654321";
//    private static final JsonSoknadsmottaker SOKNADSMOTTAKER = new JsonSoknadsmottaker()
//            .withNavEnhetsnavn(ENHETSNAVN + ", " + KOMMUNENAVN)
//            .withEnhetsnummer(ENHETSNR)
//            .withKommunenummer(KOMMUNENR);
//
//
//    private static final JsonSoknadsmottaker SOKNADSMOTTAKER_2 = new JsonSoknadsmottaker()
//            .withNavEnhetsnavn(ENHETSNAVN_2 + ", " + KOMMUNENAVN_2)
//            .withEnhetsnummer(ENHETSNR_2)
//            .withKommunenummer(KOMMUNENR_2);
//
//    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG = new AdresseForslag();
//    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG_2 = new AdresseForslag();
//    private static final AdresseForslag SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA = new AdresseForslag();
//
//    private static final NavEnhet NAV_ENHET = new NavEnhet(ENHETSNR, ENHETSNAVN, null, ORGNR);
//    private static final NavEnhet NAV_ENHET_2 = new NavEnhet(ENHETSNR_2, ENHETSNAVN_2, null, ORGNR_2);
//    private static final String EIER = "123456789101";
//
//    static {
//        SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning = ENHETSNAVN;
//        SOKNADSMOTTAKER_FORSLAG.kommunenavn = KOMMUNENAVN;
//        SOKNADSMOTTAKER_FORSLAG.kommunenummer = KOMMUNENR;
//
//        SOKNADSMOTTAKER_FORSLAG_2.geografiskTilknytning = ENHETSNAVN_2;
//        SOKNADSMOTTAKER_FORSLAG_2.kommunenavn = KOMMUNENAVN_2;
//        SOKNADSMOTTAKER_FORSLAG_2.kommunenummer = KOMMUNENR_2;
//
//        SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA.geografiskTilknytning = BYDEL_MARKA;
//        SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA.kommunenavn = KOMMUNENAVN_2;
//        SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA.kommunenummer = KOMMUNENR_2;
//    }
//
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//
//    @Mock
//    private AdresseSokService adresseSokService;
//
//    @Mock
//    private NavEnhetService navEnhetService;
//
//    @Mock
//    private KommuneInfoService kommuneInfoService;
//
//    @Mock
//    private BydelService bydelService;
//
//    @Mock
//    private GeografiskTilknytningService geografiskTilknytningService;
//
//    @Mock
//    private KodeverkService kodeverkService;
//
//    @InjectMocks
//    private NavEnhetRessurs navEnhetRessurs;
//
//    @BeforeEach
//    public void setUp() {
//        System.setProperty("environment.name", "test");
//        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
//    }
//
//    @AfterEach
//    public void tearDown() {
//        SubjectHandler.resetOidcSubjectHandlerService();
//        System.clearProperty("environment.name");
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseFraAdressesok_skalReturnereEnheterRiktigKonvertert() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//
//        when(adresseSokService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq("soknad")))
//                .thenReturn(Arrays.asList(SOKNADSMOTTAKER_FORSLAG, SOKNADSMOTTAKER_FORSLAG_2));
//        when(navEnhetService.getEnhetForGt(ENHETSNAVN)).thenReturn(NAV_ENHET);
//        when(navEnhetService.getEnhetForGt(ENHETSNAVN_2)).thenReturn(NAV_ENHET_2);
//        when(kommuneInfoService.getBehandlingskommune(KOMMUNENR, KOMMUNENAVN)).thenReturn(KOMMUNENAVN);
//        when(kommuneInfoService.getBehandlingskommune(KOMMUNENR_2, KOMMUNENAVN_2)).thenReturn(KOMMUNENAVN_2);
//
//        List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//
//        assertThatEnheterAreCorrectlyConverted(navEnhetFrontends, Arrays.asList(SOKNADSMOTTAKER, SOKNADSMOTTAKER_2));
//        assertThat(navEnhetFrontends.get(0).valgt).isTrue();
//        assertThat(navEnhetFrontends.get(1).valgt).isFalse();
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseFraAdressesok_skalReturnereEnheterRiktigKonvertertVedBydelMarka() {
//        var annenBydel = "030112";
//        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER_2).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//        when(adresseSokService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq("soknad")))
//                .thenReturn(singletonList(SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA));
//        when(bydelService.getBydelTilForMarka(SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA)).thenReturn(annenBydel);
//        when(navEnhetService.getEnhetForGt(annenBydel)).thenReturn(NAV_ENHET_2);
//        when(kommuneInfoService.getBehandlingskommune(KOMMUNENR_2, KOMMUNENAVN_2)).thenReturn(KOMMUNENAVN_2);
//
//        var navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//
//        assertThatEnheterAreCorrectlyConverted(navEnhetFrontends, singletonList(SOKNADSMOTTAKER_2));
//        assertThat(navEnhetFrontends.get(0).valgt).isTrue();
//    }
//
//    @Test
//    void hentValgtNavEnhet_skalReturnereEnhetRiktigKonvertert() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//
//        NavEnhetFrontend navEnhetFrontend = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID);
//
//        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, SOKNADSMOTTAKER);
//        assertThat(navEnhetFrontend.valgt).isTrue();
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseIkkeValgt_skalReturnereTomListe() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//        when(adresseSokService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq(null))).thenReturn(new ArrayList<>());
//
//        List<NavEnhetFrontend> navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//
//        assertThat(navEnhetFrontends).isEmpty();
//    }
//
//    @Test
//    void getValgtNavEnhet_oppholdsadresseIkkeValgt_skalReturnereNull() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//
//        NavEnhetFrontend navEnhetFrontends = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID);
//
//        assertThat(navEnhetFrontends).isNull();
//    }
//
//    @Test
//    void updateNavEnhet_skalSetteNavEnhet() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//        NavEnhetFrontend navEnhetFrontend = new NavEnhetFrontend()
//                .withEnhetsnavn(ENHETSNAVN_2)
//                .withKommunenavn(KOMMUNENAVN_2)
//                .withOrgnr(ORGNR_2)
//                .withEnhetsnr(ENHETSNR_2);
//
//        navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend);
//
//        SoknadUnderArbeid updatedSoknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
//        JsonSoknadsmottaker jsonSoknadsmottaker = updatedSoknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker();
//        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, jsonSoknadsmottaker);
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeKommunenummerFraGtOgKommunenavnFraKodeverk() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//        when(geografiskTilknytningService.hentGeografiskTilknytning(anyString())).thenReturn(OPPHOLDSADRESSE_KOMMUNENR);
//        when(navEnhetService.getEnhetForGt(OPPHOLDSADRESSE_KOMMUNENR)).thenReturn(NAV_ENHET);
//        when(kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR)).thenReturn(KOMMUNENAVN);
//        when(kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN)).thenReturn(KOMMUNENAVN);
//
//        var navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//        assertThat(navEnhetFrontends).hasSize(1);
//        assertThat(navEnhetFrontends.get(0).kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR);
//        assertThat(navEnhetFrontends.get(0).kommunenavn).isEqualTo(KOMMUNENAVN);
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeBydelsnummerFraGtOgKommunenavnFraKodeverk() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//        when(geografiskTilknytningService.hentGeografiskTilknytning(anyString())).thenReturn(OPPHOLDSADRESSE_BYDELSNR);
//        when(navEnhetService.getEnhetForGt(OPPHOLDSADRESSE_BYDELSNR)).thenReturn(NAV_ENHET);
//        when(kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR)).thenReturn(KOMMUNENAVN);
//        when(kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN)).thenReturn(KOMMUNENAVN);
//
//        var navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//        assertThat(navEnhetFrontends).hasSize(1);
//        assertThat(navEnhetFrontends.get(0).kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR);
//        assertThat(navEnhetFrontends.get(0).kommunenavn).isEqualTo(KOMMUNENAVN);
//    }
//
//    @Test
//    void hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeAdressesokSomFallback() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().withMottaker(SOKNADSMOTTAKER).getData().getPersonalia()
//                .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
//        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
//
//        when(geografiskTilknytningService.hentGeografiskTilknytning(anyString())).thenThrow(new PdlApiException("pdl feil"));
//
//        when(adresseSokService.finnAdresseFraSoknad(any(JsonPersonalia.class), eq("folkeregistrert")))
//                .thenReturn(singletonList(SOKNADSMOTTAKER_FORSLAG));
//
//        when(navEnhetService.getEnhetForGt(SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning)).thenReturn(NAV_ENHET);
//
//        var navEnhetFrontends = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID);
//        assertThat(navEnhetFrontends).hasSize(1);
//        assertThat(navEnhetFrontends.get(0).kommuneNr).isEqualTo(KOMMUNENR);
//
//        verifyNoInteractions(kodeverkService);
//    }
//
//    @Test
//    void hentNavEnheter_skalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> navEnhetRessurs.hentNavEnheter(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void hentValgtNavEnhet_skalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    @Test
//    void updateNavEnhet_skalKasteAuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
//
//        var navEnhetFrontend = new NavEnhetFrontend();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend));
//
//        verifyNoInteractions(soknadUnderArbeidRepository);
//    }
//
//    private void assertThatEnheterAreCorrectlyConverted(List<NavEnhetFrontend> navEnhetFrontends, List<JsonSoknadsmottaker> jsonSoknadsmottakers) {
//        for (int i = 0; i < navEnhetFrontends.size(); i++) {
//            assertThatEnhetIsCorrectlyConverted(navEnhetFrontends.get(i), jsonSoknadsmottakers.get(i));
//        }
//    }
//
//    private void assertThatEnhetIsCorrectlyConverted(NavEnhetFrontend navEnhetFrontend, JsonSoknadsmottaker soknadsmottaker) {
//        if (navEnhetFrontend == null) {
//            assertThat(soknadsmottaker).isNull();
//            return;
//        }
//
//        String kombinertnavn = soknadsmottaker.getNavEnhetsnavn();
//        String enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','));
//        String kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2);
//
//        assertThat(navEnhetFrontend.enhetsnavn).isEqualTo(enhetsnavn);
//        assertThat(navEnhetFrontend.kommunenavn).isEqualTo(kommunenavn);
//        assertThat(navEnhetFrontend.enhetsnr).isEqualTo(soknadsmottaker.getEnhetsnummer());
//    }
//
//    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
//        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
//        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
//        return argument.getValue();
//    }
//}