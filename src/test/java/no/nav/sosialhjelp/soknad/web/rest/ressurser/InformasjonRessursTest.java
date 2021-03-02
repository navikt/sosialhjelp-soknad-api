package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.business.service.InformasjonService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon.InformasjonRessurs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private PdlService pdlService;

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
    public void skalHenteTeksterForForeldrepengerViaBundleForeldrepenger() {
        ressurs.hentTekster("foreldrepenger", null);
        verify(messageSource).getBundleFor("foreldrepenger", norskBokmaal);
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
}
