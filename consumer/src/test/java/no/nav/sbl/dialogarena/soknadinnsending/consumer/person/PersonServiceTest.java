package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonServiceTest {
    @InjectMocks
    private PersonService personService;
    @Mock
    private PersonPortType personPortType;

    @Test(expected = IkkeFunnetException.class)
    public void skalKasteIkkeFunnetExceptionHvisPersonIkkeFinnesITps() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonPersonIkkeFunnet("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void skalKasteSikkerhetsBegrensningExceptionHvisTpsNekterTilgangTilBruker() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new HentKjerneinformasjonSikkerhetsbegrensning("", null));
        personService.hentKjerneinformasjon("");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalKasteTjenesteUtilgjengeligExceptionHvisTpsErNede() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        when(personPortType.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new WebServiceException("", null));
        personService.hentKjerneinformasjon("");
    }
}
