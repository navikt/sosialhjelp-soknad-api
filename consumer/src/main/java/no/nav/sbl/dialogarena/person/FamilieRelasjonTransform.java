package no.nav.sbl.dialogarena.person;

import java.util.ArrayList;
import java.util.List;

import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

public class FamilieRelasjonTransform {

	public Person mapFamilierelasjonTilPerson(Long soknadId,
			HentKjerneinformasjonResponse response) {
		if (response == null) {
            return new Person();
        }
		
		no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson = response.getPerson();
		Person person = new Person(
	                soknadId,
	                finnFnr(xmlperson),
	                finnFornavn(xmlperson),
	                finnMellomNavn(xmlperson),
	                finnEtterNavn(xmlperson),
	                finnBarn(xmlperson, soknadId));
	        
	    return person;
	}
	
    private List<Person> finnBarn(
			no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson, Long soknadId) {
		List<Person> result = new ArrayList<Person>();
		
		List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
		if(familierelasjoner.size() == 0) {
			return result;
		}
		
		for (Familierelasjon familierelasjon : familierelasjoner) {
			Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
			
			//TODO: Kodeverk
			if (familierelasjonType.getValue().equals("forelder")) {
				no.nav.tjeneste.virksomhet.person.v1.informasjon.Person tilPerson = familierelasjon.getTilPerson();
				Person barn = mapXmlPersonToPerson(tilPerson, soknadId);
				result.add(barn);
			}
		}
		
		return result;
	}

	private Person mapXmlPersonToPerson(
			no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson, Long soknadId) {
		Person person = new Person(
                soknadId,
                finnFnr(xmlperson),
                finnFornavn(xmlperson),
                finnMellomNavn(xmlperson),
                finnEtterNavn(xmlperson),
                finnBarn(xmlperson, soknadId));
		return person;
	}

	private String finnFornavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private boolean fornavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private String finnMellomNavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private boolean mellomnavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private String finnEtterNavn(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private boolean etternavnExists(no.nav.tjeneste.virksomhet.person.v1.informasjon.Person soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }

	private String finnFnr(
			no.nav.tjeneste.virksomhet.person.v1.informasjon.Person xmlperson) {
		return xmlperson.getIdent().getIdent();
	}	
}
