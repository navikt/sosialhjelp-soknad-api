package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursTest {

    @Spy
    InformasjonService informasjonService;
    @Spy
    SoknadService soknadService;
    @Spy
    LandService landService;
    @Mock
    PersonaliaService personaliaService;
    @Mock
    PersonInfoService personInfoService;
    @Mock
    ThreadLocalSubjectHandler subjectHandler;

    @InjectMocks
    InformasjonRessurs ressurs;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
        when(personInfoService.hentArbeidssokerStatus(anyString())).thenReturn("ARBS");
        when(personaliaService.hentPersonalia(anyString())).thenReturn(personalia());
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
        Map<String, String> utslagskriterier = ressurs.hentUtslagskriterier();
        assertThat(utslagskriterier.containsKey("arbeidssokerstatus")).isTrue();
        assertThat(utslagskriterier.containsKey("alder")).isTrue();
        assertThat(utslagskriterier.containsKey("bosattINorge")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresse")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigFra")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigTil")).isTrue();
    }

    private Personalia personalia() {
        Personalia personalia = new Personalia();
        personalia.setFnr("***REMOVED***");
        Adresse adresse = new Adresse();
        personalia.setGjeldendeAdresse(adresse);
        return personalia;
    }

}
