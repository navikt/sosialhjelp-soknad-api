package no.nav.sbl.dialogarena.soknadinnsending.consumer.personv3;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonServiceV3Test {
    @InjectMocks
    private PersonServiceV3 personService;
    @Mock
    private PersonV3 personV3;
    @Mock
    private Kodeverk kodeverk;



    @Test(expected = IkkeFunnetException.class)
    public void skalKasteIkkeFunnetExceptionHvisPersonIkkeFinnesITps() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonPersonIkkeFunnet("", null));
        personService.hentAddresserOgKontonummer("");
    }

    @Test(expected = SikkerhetsBegrensningException.class)
    public void skalKasteSikkerhetsBegrensningExceptionHvisTpsNekterTilgangTilBruker() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new HentPersonSikkerhetsbegrensning("", null));
        personService.hentAddresserOgKontonummer("");
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void skalKasteTjenesteUtilgjengeligExceptionHvisTpsErNede() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenThrow(new WebServiceException("", null));
        personService.hentAddresserOgKontonummer("");
    }

    @Test
    public void skalTakleTomRespons() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse());
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer).isNotNull();
    }

    @Test
    public void skalIkkeVisePersonerMedDiskresjonsKode6() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Person()
                .withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(new Gateadresse()
                        .withPoststed(new Postnummer().withValue("Oslo"))
                        .withGatenavn("Veien")))
                .withDiskresjonskode(new Diskresjonskoder().withValue("SPSF"))));
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse()).isNull();
    }

    @Test
    public void skalIkkeVisePersonerMedDiskresjonsKode7() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Person()
                .withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(new Gateadresse()
                        .withPoststed(new Postnummer().withValue("Oslo"))
                        .withGatenavn("Veien")))
                .withDiskresjonskode(new Diskresjonskoder().withValue("SPFO"))));
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse()).isNull();
    }

    @Test
    public void skalVisePersonerUtenDiskresjonsKode() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Person()
                .withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(new Gateadresse()
                        .withPoststed(new Postnummer().withValue("Oslo"))
                        .withGatenavn("Veien")))
        ));
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse()).isNotNull();
    }

    @Test
    public void skalHenteMatrikkelAdresseMedKommunenr() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Person()
                .withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(new Matrikkeladresse()
                        .withPoststed(new Postnummer().withValue("Oslo"))
                        .withKommunenummer("0301")))
        ));
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse().getStrukturertAdresse().kommunenummer).isEqualTo("0301");
    }

    @Test
    public void skalHenteKontonummer() throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        when(personV3.hentPerson(any(HentPersonRequest.class))).thenReturn(new HentPersonResponse().withPerson(new Bruker()
                .withBankkonto(new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer("00000000000")))
        ));
        AdresserOgKontonummer adresserOgKontonummer = personService.hentAddresserOgKontonummer("");
        assertThat(adresserOgKontonummer.getKontonummer()).isEqualTo("00000000000");
    }
}
