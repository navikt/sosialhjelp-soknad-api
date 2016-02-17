package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import javax.xml.ws.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PersonServiceTest {
    @InjectMocks
    private PersonService personService;
    @Mock
    private PersonPortType personPortType;

    @Test(expected = IkkeFunnetException.class)
    public void skalWrappeExceptions() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonPersonIkkeFunnet("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void skalWrappeExceptions2() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonSikkerhetsbegrensning("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalWrappeExceptions3() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new WebServiceException("", null));
        personService.hentKjerneinformasjon("");
    }


}
