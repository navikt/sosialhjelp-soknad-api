package no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.service.InformasjonService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursTest {

    public static final String SOKNADSTYPE = "type";

    @Spy
    private InformasjonService informasjonService;
    @Spy
    private SoknadService soknadService;
    @Mock
    private NavMessageSource messageSource;
    @Mock
    private KommuneInfoService kommuneInfoService;
    @Mock
    private PersonService personService;
    @Mock
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private InformasjonRessurs ressurs;

    Locale norskBokmaal = new Locale("nb", "NO");

    @Before
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    public void miljovariablerInneholderAlleVariableneViTrenger() {
        Map<String, String> miljovariabler = ressurs.hentMiljovariabler();

        assertThat(miljovariabler).containsKey("dittnav.link.url");
        assertThat(miljovariabler).containsKey("soknad.ettersending.antalldager");
    }

    @Test
    public void spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
        ressurs.hentTekster(SOKNADSTYPE, null);
        ressurs.hentTekster(SOKNADSTYPE, " ");
        verify(messageSource, times(2)).getBundleFor(SOKNADSTYPE, norskBokmaal);
    }

    @Test
    public void skalHenteTeksterForSoknadsosialhjelpViaBundle() {
        ressurs.hentTekster("soknadsosialhjelp", null);
        verify(messageSource).getBundleFor("soknadsosialhjelp", norskBokmaal);
    }

    @Test
    public void skalHenteTeksterForAlleBundlesUtenType() {
        ressurs.hentTekster("", null);
        verify(messageSource).getBundleFor("", norskBokmaal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void kastExceptionHvisIkkeSpraakErPaaRiktigFormat() {
        ressurs.hentTekster(SOKNADSTYPE, "NORSK");
    }

    @Test
    public void skalHenteAllePaakobledeKommuner() {
        String manueltPaakobletKommune = "0301";
        String digisosKommune = "asdf";
        String deaktivertDigisosKommune = "456456";

        Map<String, KommuneInfo> map = new HashMap<>();
        map.put(manueltPaakobletKommune, null);
        map.put(digisosKommune, null);
        map.put(deaktivertDigisosKommune, null);

        when(kommuneInfoService.hentAlleKommuneInfo()).thenReturn(map);

        when(kommuneInfoService.kanMottaSoknader(manueltPaakobletKommune)).thenReturn(true);
        when(kommuneInfoService.kanMottaSoknader(digisosKommune)).thenReturn(true);
        when(kommuneInfoService.kanMottaSoknader(deaktivertDigisosKommune)).thenReturn(false);

        Set<String> tilgjengeligeKommuner = ressurs.hentTilgjengeligeKommuner();

        assertThat(tilgjengeligeKommuner)
                .hasSize(KommuneTilNavEnhetMapper.getDigisoskommuner().size() + 1)
                .contains(manueltPaakobletKommune)
                .contains(digisosKommune)
                .doesNotContain(deaktivertDigisosKommune);
    }

    @Test
    public void skalReturnereMappetListeOverManueltPakobledeKommuner() {
        List<String> manuelleKommuner = singletonList("1234");
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner);

        assertNotNull(mappedeKommuner.get("1234"));
        assertTrue(mappedeKommuner.get("1234").kanMottaSoknader);
        assertFalse(mappedeKommuner.get("1234").kanOppdatereStatus);
    }

    @Test
    public void skalReturnereMappetListeOverDigisosKommuner() {
        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = ressurs.mapDigisosKommuner(digisosKommuner);

        assertNotNull(mappedeKommuner.get("1234"));
        assertTrue(mappedeKommuner.get("1234").kanMottaSoknader);
        assertTrue(mappedeKommuner.get("1234").kanOppdatereStatus);
    }

    @Test
    public void duplikatIDigisosKommuneSkalOverskriveManuellKommune() {
        List<String> manuelleKommuner = singletonList("1234");
        Map<String, InformasjonRessurs.KommuneInfoFrontend> manueltMappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner);
        assertFalse(manueltMappedeKommuner.get("1234").kanOppdatereStatus); // Manuelle kommuner får ikke innsyn

        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeDigisosKommuner = ressurs.mapDigisosKommuner(digisosKommuner);

        Map<String, InformasjonRessurs.KommuneInfoFrontend> margedKommuner = ressurs.mergeManuelleKommunerMedDigisosKommuner(manueltMappedeKommuner, mappedeDigisosKommuner);
        assertThat(margedKommuner).hasSize(1);
        assertTrue(margedKommuner.get("1234").kanOppdatereStatus);
    }

    @Test(expected = AuthorizationException.class)
    public void harNyligInnsendteSoknader_AuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        ressurs.harNyligInnsendteSoknader();

        verifyNoInteractions(soknadMetadataRepository);
    }

    @Test
    public void harNyligInnsendteSoknader_tomResponse() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
                .thenReturn(Collections.emptyList());

        var response = ressurs.harNyligInnsendteSoknader();

        assertThat(response.getAntallNyligInnsendte()).isZero();
    }

    @Test
    public void harNyligInnsendteSoknader_tomResponse_null() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
                .thenReturn(null);

        var response = ressurs.harNyligInnsendteSoknader();

        assertThat(response.getAntallNyligInnsendte()).isZero();
    }

    @Test
    public void harNyligInnsendteSoknader_flereSoknaderResponse() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any()))
                .thenReturn(Arrays.asList(mock(SoknadMetadata.class), mock(SoknadMetadata.class)));

        var response = ressurs.harNyligInnsendteSoknader();

        assertThat(response.getAntallNyligInnsendte()).isEqualTo(2);
    }
}
