package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.InformasjonRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid.ArbeidssokerInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursTest {

    public static final String SOKNADSTYPE = "type";
    public static final String TEMAKODE = "TEMAKODE";

    @Spy
    InformasjonService informasjonService;
    @Spy
    SoknadService soknadService;
    @Spy
    LandService landService;
    @Mock
    PersonaliaBolk personaliaBolk;
    @Mock
    PersonInfoService personInfoService;
    @Mock
    ThreadLocalSubjectHandler subjectHandler;
    @Mock
    NavMessageSource messageSource;
    @Mock
    WebSoknadConfig soknadConfig;
    @Mock
    ArbeidssokerInfoService arbeidssokerInfoService;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;


    @InjectMocks
    InformasjonRessurs ressurs;

    Locale norskBokmaal = new Locale("nb", "NO");
    SoknadStruktur struktur;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
        when(personInfoService.hentArbeidssokerStatus(anyString())).thenReturn("ARBS");
        when(personaliaBolk.hentPersonalia(anyString())).thenReturn(personalia());

        KravdialogInformasjonHolder kravdialogInformasjonHolder = new KravdialogInformasjonHolder();
        when(this.kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()).thenReturn(kravdialogInformasjonHolder.getSoknadsKonfigurasjoner());


        struktur = new SoknadStruktur();
        struktur.setTemaKode(TEMAKODE);
        struktur.setFakta(singletonList(new FaktumStruktur()));
        when(soknadConfig.hentStruktur(anyString())).thenReturn(struktur);
    }

    @Test
    public void miljovariablerInneholderAlleVariableneViTrenger() {
        Map<String, String> miljovariabler = ressurs.hentMiljovariabler();

        assertThat(miljovariabler.containsKey("saksoversikt.link.url")).isTrue();
        assertThat(miljovariabler.containsKey("dittnav.link.url")).isTrue();
        assertThat(miljovariabler.containsKey("dialogarena.navnolink.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.skjemaveileder.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.alderspensjon.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.reelarbeidsoker.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.dagpengerbrosjyre.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.brukerprofil.url")).isTrue();
        assertThat(miljovariabler.containsKey("dialogarena.cms.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknadinnsending.soknad.path")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.ettersending.antalldager")).isTrue();
    }

    @Test
    public void statsborgerskapTypeSkalReturnereEosVedSvensk() {
        Map<String, String> statsborgerskapType = ressurs.hentStatsborgerskapstype("SWE");
        assertThat(statsborgerskapType.get("result")).isEqualTo("eos");
    }

    @Test
    public void utslagskriterierInneholderAlleKriteriene() {
        when(arbeidssokerInfoService.getArbeidssokerArenaStatus(anyString())).thenReturn("ARBS");
        Map<String, Object> utslagskriterier = ressurs.hentUtslagskriterier();
        assertThat(utslagskriterier.containsKey("arbeidssokertatusFraSBLArbeid")).isTrue();
        assertThat(utslagskriterier.containsKey("arbeidssokerstatus")).isTrue();
        assertThat(utslagskriterier.containsKey("ytelsesstatus")).isTrue();
        assertThat(utslagskriterier.containsKey("alder")).isTrue();
        assertThat(utslagskriterier.containsKey("fodselsdato")).isTrue();
        assertThat(utslagskriterier.containsKey("bosattINorge")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresse")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigFra")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigTil")).isTrue();
        assertThat(utslagskriterier.containsKey("erBosattIEOSLand")).isTrue();
        assertThat(utslagskriterier.containsKey("statsborgerskap")).isTrue();

        assertThat(utslagskriterier.size()).isEqualTo(11);
    }

    @Test
    public void spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
        ressurs.hentTekster(SOKNADSTYPE, null);
        ressurs.hentTekster(SOKNADSTYPE, " ");
        verify(messageSource, times(2)).getBundleFor(SOKNADSTYPE, norskBokmaal);
    }

    @Test
    public void skalHenteTeksterForAapViaBundleSoknadaap() {
        ressurs.hentTekster("AAP", null);
        verify(messageSource).getBundleFor("soknadaap", norskBokmaal);
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
    public void returnerFullStrukturHvisIkkeFilterErSatt() {
        SoknadStruktur struktur = ressurs.hentSoknadStruktur("NAV123", null);
        assertThat(struktur.getTemaKode()).isEqualTo(TEMAKODE);
        assertThat(struktur.getFakta()).isNotEmpty();
    }

    @Test
    public void returnerStrukturMedBareTemakodeHvisFilterErSattTilTemakode() {
        SoknadStruktur struktur = ressurs.hentSoknadStruktur("NAV123", "temakode");
        assertThat(struktur.getTemaKode()).isEqualTo(TEMAKODE);
        assertThat(struktur.getFakta()).isEmpty();
    }

    private Personalia personalia() {
        Personalia personalia = new Personalia();
        personalia.setFnr("***REMOVED***");
        Adresse adresse = new Adresse();
        personalia.setGjeldendeAdresse(adresse);
        return personalia;
    }

}
