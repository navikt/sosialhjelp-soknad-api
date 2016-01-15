package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;


public class PersonMock {
    private static PersonMock personMock = new PersonMock();
    private PersonPortTypeMock personPortTypeMock;

    PersonMock(){
        personPortTypeMock = new PersonPortTypeMock();
        Person person = genererPersonMedGyldigIdentOgNavn("03076321565", "person", "mock");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();

        Doedsdato doedsdato = new Doedsdato();
        doedsdato.setDoedsdato(XMLGregorianCalendarImpl.createDate(2014, 2, 2, 0));
        familieRelasjoner.add(lagBarn("01010091736", "Dole", "Mockmann", doedsdato));
        familieRelasjoner.add(lagBarn("03060193877", "Ole", "Mockmann"));
        familieRelasjoner.add(lagBarn("03060194075", "Doffen", "Mockmann"));

        personPortTypeMock.setPerson(person);
    }

    public static PersonMock getInstance(){
        return personMock;
    }

    public PersonPortTypeMock personMock() {
        return personPortTypeMock;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn){
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn = genererPersonMedGyldigIdentOgNavn(fnr, fornavn, etternavn);
        familierelasjon.setTilPerson(barn);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);
        return familierelasjon;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn, Doedsdato doedsdato){
        Familierelasjon familierelasjon = lagBarn(fnr, fornavn, etternavn);
        familierelasjon.getTilPerson().setDoedsdato(doedsdato);
        return familierelasjon;
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

    private Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        try {
            foedselsdato.setFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(year, month, day, 0));
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Klarte ikke å sette fødselsdato", e);
        }
        return foedselsdato;
    }

}
