package no.nav.sbl.dialogarena.person;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

/**
 * Map from TPS data format to internal domain model
 *
 */
public class PersonTransform {

	public Person mapToPerson(Long soknadId, XMLHentKontaktinformasjonOgPreferanserResponse response) {
        if (response == null) {
        	return new Person(); 
        }
		XMLBruker soapPerson = (XMLBruker) response.getPerson();

        return new Person(soknadId, finnFnr(soapPerson), finnSammensattNavn(soapPerson), finnAdresser(soknadId, soapPerson));
    }

    private List<PersonAdresse> finnAdresser(long soknadId, XMLBruker soapPerson) {
		List<PersonAdresse> result = new ArrayList<PersonAdresse>();
    	XMLBostedsadresse bostedsadresse = soapPerson.getBostedsadresse();
    	if (bostedsadresse != null) {
			XMLStrukturertAdresse strukturertAdresse = bostedsadresse.getStrukturertAdresse();
			
			if(strukturertAdresse instanceof XMLGateadresse) {
				String xmlAdressetype = soapPerson.getGjeldendePostadresseType().getValue();
	
				XMLGateadresse xmlGateAdresse = (XMLGateadresse)strukturertAdresse;
				
				String gatenummerString = getGatenummer(xmlGateAdresse);
				
				String postnummerString = getPostnummerString(xmlGateAdresse);
				PersonAdresse personAdresse = new PersonAdresse(soknadId, Adressetype.valueOf(xmlAdressetype), 
						xmlGateAdresse.getGatenavn(), gatenummerString, postnummerString);
				result.add(personAdresse);
			}
    	}
    	XMLMidlertidigPostadresse midlertidigPostadresse = soapPerson.getMidlertidigPostadresse();
    	if(midlertidigPostadresse != null) {
    		if(midlertidigPostadresse instanceof XMLMidlertidigPostadresse) {
    			XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
    			XMLStrukturertAdresse strukturertAdresse = xmlMidlPostAdrNorge.getStrukturertAdresse();
    			if(strukturertAdresse instanceof XMLGateadresse) {
    				XMLGateadresse xmlGateAdresse = (XMLGateadresse) strukturertAdresse;
    				
    				String gatenummerString = getGatenummer(xmlGateAdresse);
    				String postnummerString = getPostnummerString(xmlGateAdresse);
    				
    				PersonAdresse personAdresse = new PersonAdresse(soknadId, Adressetype.MIDLERTIDIG_ADRESSE_NORGE,
    						xmlGateAdresse.getGatenavn(), gatenummerString, postnummerString);
    				result.add(personAdresse);
    			}
    		}
//    		if(midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
//    			XMLMidlertidigPostadresseUtland xmlMidlAdrUtland = (XMLMidlertidigPostadresseUtland) midlertidigPostadresse;
//    			XMLUstrukturertAdresse ustrukturertAdresse = xmlMidlAdrUtland.getUstrukturertAdresse();
//    			ustrukturertAdresse.getAdresselinje1();
//    		}
    	}
		return result;
	}

	private String getPostnummerString(XMLGateadresse xmlGateAdresse) {
		XMLPostnummer postnummer = xmlGateAdresse.getPoststed();
		if(postnummer != null) {
			return  postnummer.getValue();
		}
		return "";
	}

	private String getGatenummer(XMLGateadresse xmlGateAdresse) {
		BigInteger gatenummer = xmlGateAdresse.getGatenummer();
		if(gatenummer != null) {
			return gatenummer.toString();
		}
		return "";
	}

	private String finnFnr(XMLBruker soapPerson) {
    	return soapPerson.getIdent().getIdent();
	}

	private String finnSammensattNavn(XMLBruker soapPerson) {
		if(soapPerson.getPersonnavn() != null) {
			return soapPerson.getPersonnavn().getSammensattNavn();
		} else {
			return "";
		}
    }
}
