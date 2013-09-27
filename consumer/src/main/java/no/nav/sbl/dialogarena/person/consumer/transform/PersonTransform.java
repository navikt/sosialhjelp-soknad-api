package no.nav.sbl.dialogarena.person.consumer.transform;



import no.nav.sbl.dialogarena.person.Person;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

/**
 * Map from TPS data format to internal domain model
 *
 */
public class PersonTransform {

	public Person mapToPerson(XMLHentKontaktinformasjonOgPreferanserResponse response) {
        XMLBruker soapPerson = (XMLBruker) response.getPerson();
        //Do some converting to domeneobjekt
        return Person.identifisert(finnEtterNavn(soapPerson), finnFnr(soapPerson));
    }

    private String finnFnr(XMLBruker soapPerson) {
    	return soapPerson.getIdent().toString();
	}

	private String finnEtterNavn(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn().getEtternavn();
    }
}
