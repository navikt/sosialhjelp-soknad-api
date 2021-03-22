package no.nav.sosialhjelp.soknad.consumer.personv3;

import no.nav.sosialhjelp.soknad.consumer.exceptions.IkkeFunnetException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.domain.model.Kontonummer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonServiceV3Test {
    @InjectMocks
    private PersonServiceV3 personService;
    @Mock
    private PersonV3 personV3;
    @Mock
    private KodeverkService kodeverkService;

    @Test(expected = IkkeFunnetException.class)
    public void skalKasteIkkeFunnetExceptionHvisPersonIkkeFinnesITps() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet("", null));
        personService.hentKontonummer("");
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void skalKasteSikkerhetsBegrensningExceptionHvisTpsNekterTilgangTilBruker() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning("", null));
        personService.hentKontonummer("");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalKasteTjenesteUtilgjengeligExceptionHvisTpsErNede() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new WebServiceException("", null));
        personService.hentKontonummer("");
    }

    @Test
    public void skalTakleTomRespons() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse());
        Kontonummer kontonummer = personService.hentKontonummer("");
        assertThat(kontonummer).isNotNull();
    }

    @Test
    public void skalHenteKontonummer() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Bruker()
                .withBankkonto(new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer("00000000000")))
        ));
        Kontonummer adresserOgKontonummer = personService.hentKontonummer("");
        assertThat(adresserOgKontonummer.getKontonummer()).isEqualTo("00000000000");
    }
}
