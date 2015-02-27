package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonMock {

    public PersonPortType personMock() {
        PersonPortType mock = mock(PersonPortType.class);
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        Person person = genererPersonMedGyldigIdentOgNavn("03076321565", "person", "mock");

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("DNK");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();

        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn("01010091736", "Dole", "Mockmann");
        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);
        familieRelasjoner.add(familierelasjon);

        Familierelasjon familierelasjon2 = new Familierelasjon();
        Person barn2 = genererPersonMedGyldigIdentOgNavn("03060193877", "Ole", "Mockmann");
        Doedsdato doedsdato = new Doedsdato();
        doedsdato.setDoedsdato(XMLGregorianCalendarImpl.createDate(2014, 2, 2, 0));
        barn2.setDoedsdato(doedsdato);
        familierelasjon2.setTilPerson(barn2);
        Familierelasjoner familieRelasjonRolle2 = new Familierelasjoner();
        familieRelasjonRolle2.setValue("BARN");
        familierelasjon2.setTilRolle(familieRelasjonRolle2);
        familieRelasjoner.add(familierelasjon2);

        Familierelasjon familierelasjon3 = new Familierelasjon();
        Person barn3 = genererPersonMedGyldigIdentOgNavn("03060194075", "Doffen", "Mockmann");
        familierelasjon3.setTilPerson(barn3);
        Familierelasjoner familieRelasjonRolle3 = new Familierelasjoner();
        familieRelasjonRolle3.setValue("BARN");
        familierelasjon3.setTilRolle(familieRelasjonRolle3);
        familieRelasjoner.add(familierelasjon3);

        response.setPerson(person);

        try {
            when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenReturn(response);
        } catch (HentKjerneinformasjonPersonIkkeFunnet ikkeFunnet) {
            throw new RuntimeException(ikkeFunnet);
        } catch (HentKjerneinformasjonSikkerhetsbegrensning sikkerhetsbegrensning) {
            throw new RuntimeException(sikkerhetsbegrensning);
        }
        //Mockito.doThrow(new RuntimeException()).when(mock).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
        //when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new WebServiceException());

        return mock;
    }

    private Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String etternavn) {
        Person xmlPerson = new Person();

        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        personnavn.setMellomnavn("");
        personnavn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(personnavn);

        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(ident);
        xmlPerson.setIdent(norskIdent);

        return xmlPerson;
    }

}
