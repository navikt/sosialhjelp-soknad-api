//package no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon;
//
//import no.nav.sosialhjelp.api.fiks.KommuneInfo;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService;
//import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.personalia.person.PersonService;
//import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import static java.util.Collections.singletonList;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class InformasjonRessursTest {
//
//    public static final String SOKNADSTYPE = "type";
//
//    @Mock
//    private NavMessageSource messageSource;
//    @Mock
//    private KommuneInfoService kommuneInfoService;
//    @Mock
//    private PersonService personService;
//    @Mock
//    private Tilgangskontroll tilgangskontroll;
//    @Mock
//    private SoknadMetadataRepository soknadMetadataRepository;
//
//    @InjectMocks
//    private InformasjonRessurs ressurs;
//
//    Locale norskBokmaal = new Locale("nb", "NO");
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
//    void spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
//        ressurs.hentTekster(SOKNADSTYPE, null);
//        ressurs.hentTekster(SOKNADSTYPE, " ");
//        verify(messageSource, times(2)).getBundleFor(SOKNADSTYPE, norskBokmaal);
//    }
//
//    @Test
//    void skalHenteTeksterForSoknadsosialhjelpViaBundle() {
//        ressurs.hentTekster("soknadsosialhjelp", null);
//        verify(messageSource).getBundleFor("soknadsosialhjelp", norskBokmaal);
//    }
//
//    @Test
//    void skalHenteTeksterForAlleBundlesUtenType() {
//        ressurs.hentTekster("", null);
//        verify(messageSource).getBundleFor("", norskBokmaal);
//    }
//
//    @Test
//    void kastExceptionHvisIkkeSpraakErPaaRiktigFormat() {
//        assertThatExceptionOfType(IllegalArgumentException.class)
//                .isThrownBy(() -> ressurs.hentTekster(SOKNADSTYPE, "NORSK"));
//    }
//
//    @Test
//    void skalReturnereMappetListeOverManueltPakobledeKommuner() {
//        List<String> manuelleKommuner = singletonList("1234");
//        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner);
//
//        assertThat(mappedeKommuner.get("1234")).isNotNull();
//        assertThat(mappedeKommuner.get("1234").kanMottaSoknader).isTrue();
//        assertThat(mappedeKommuner.get("1234").kanOppdatereStatus).isFalse();
//    }
//
//    @Test
//    void skalReturnereMappetListeOverDigisosKommuner() {
//        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
//        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
//        digisosKommuner.put("5678", new KommuneInfo("5678", true, true, true, false, null, false, null));
//        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = ressurs.mapDigisosKommuner(digisosKommuner);
//
//        assertThat(mappedeKommuner.get("1234")).isNotNull();
//        assertThat(mappedeKommuner.get("1234").kanMottaSoknader).isTrue();
//        assertThat(mappedeKommuner.get("1234").kanOppdatereStatus).isTrue();
//
//        assertThat(mappedeKommuner.get("5678")).isNotNull();
//        assertThat(mappedeKommuner.get("5678").kanMottaSoknader).isFalse();
//        assertThat(mappedeKommuner.get("5678").kanOppdatereStatus).isTrue();
//    }
//
//    @Test
//    void duplikatIDigisosKommuneSkalOverskriveManuellKommune() {
//        List<String> manuelleKommuner = singletonList("1234");
//        Map<String, InformasjonRessurs.KommuneInfoFrontend> manueltMappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner);
//        assertThat(manueltMappedeKommuner.get("1234").kanOppdatereStatus).isFalse(); // Manuelle kommuner får ikke innsyn
//
//        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
//        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
//        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeDigisosKommuner = ressurs.mapDigisosKommuner(digisosKommuner);
//
//        Map<String, InformasjonRessurs.KommuneInfoFrontend> margedKommuner = ressurs.mergeManuelleKommunerMedDigisosKommuner(manueltMappedeKommuner, mappedeDigisosKommuner);
//        assertThat(margedKommuner).hasSize(1);
//        assertThat(margedKommuner.get("1234").kanOppdatereStatus).isTrue();
//    }
//
//    @Test
//    void harNyligInnsendteSoknader_AuthorizationExceptionVedManglendeTilgang() {
//        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();
//
//        assertThatExceptionOfType(AuthorizationException.class)
//                .isThrownBy(() -> ressurs.harNyligInnsendteSoknader());
//
//        verifyNoInteractions(soknadMetadataRepository);
//    }
//
//    @Test
//    void harNyligInnsendteSoknader_tomResponse() {
//        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
//                .thenReturn(Collections.emptyList());
//
//        var response = ressurs.harNyligInnsendteSoknader();
//
//        assertThat(response.getAntallNyligInnsendte()).isZero();
//    }
//
//    @Test
//    void harNyligInnsendteSoknader_tomResponse_null() {
//        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
//                .thenReturn(null);
//
//        var response = ressurs.harNyligInnsendteSoknader();
//
//        assertThat(response.getAntallNyligInnsendte()).isZero();
//    }
//
//    @Test
//    void harNyligInnsendteSoknader_flereSoknaderResponse() {
//        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
//                .thenReturn(Arrays.asList(mock(SoknadMetadata.class), mock(SoknadMetadata.class)));
//
//        var response = ressurs.harNyligInnsendteSoknader();
//
//        assertThat(response.getAntallNyligInnsendte()).isEqualTo(2);
//    }
//}
