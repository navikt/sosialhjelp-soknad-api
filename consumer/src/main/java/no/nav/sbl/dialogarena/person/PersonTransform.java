package no.nav.sbl.dialogarena.person;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Map from TPS data format to internal domain model
 *
 */
public class PersonTransform {

	private Kodeverk kodeverk;
	
	public Person mapToPerson(Long soknadId, XMLHentKontaktinformasjonOgPreferanserResponse response, Kodeverk kodeverk) {
		this.kodeverk = kodeverk;
		if (response == null) {
        	return new Person(); 
        }
		XMLBruker soapPerson = (XMLBruker) response.getPerson();

        return new Person(soknadId, finnFnr(soapPerson), finnForNavn(soapPerson), finnMellomNavn(soapPerson), finnEtterNavn(soapPerson), finnAdresser(soknadId, soapPerson));
    }

	private List<Adresse> finnAdresser(long soknadId, XMLBruker soapPerson) {
		List<Adresse> result = new ArrayList<Adresse>();
    	XMLBostedsadresse bostedsadresse = soapPerson.getBostedsadresse();
    	if (bostedsadresse != null) {
			XMLStrukturertAdresse strukturertAdresse = bostedsadresse.getStrukturertAdresse();
			
			if(strukturertAdresse instanceof XMLGateadresse) {
				String xmlAdressetype = soapPerson.getGjeldendePostadresseType().getValue();
	
				XMLGateadresse xmlGateAdresse = (XMLGateadresse)strukturertAdresse;
				
				String gatenummerString = getHusnummer(xmlGateAdresse);
				String husbokstavString = getHusbokstav(xmlGateAdresse);
				
				String postnummerString = getPostnummerString(xmlGateAdresse);
				String poststed = kodeverk.getPoststed(postnummerString);
				Adresse personAdresse = new Adresse(soknadId, Adressetype.valueOf(xmlAdressetype));
				personAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
				personAdresse.setHusnummer(gatenummerString);
				personAdresse.setHusbokstav(husbokstavString);
				personAdresse.setPostnummer(postnummerString);
				personAdresse.setPoststed(poststed);
				
				result.add(personAdresse);
			}
    	}
    	XMLMidlertidigPostadresse midlertidigPostadresse = soapPerson.getMidlertidigPostadresse();
    	if(midlertidigPostadresse != null) {
    		if(midlertidigPostadresse instanceof XMLMidlertidigPostadresse) {
    			XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
    			
    			DateTime gyldigFra = new DateTime();
    			DateTime gyldigTil = new DateTime();
    			XMLGyldighetsperiode postleveringsPeriode = xmlMidlPostAdrNorge.getPostleveringsPeriode();
    			if(postleveringsPeriode != null) {
    				gyldigFra = postleveringsPeriode.getFom();
    				gyldigTil = postleveringsPeriode.getTom();
    			}
    			
    			XMLStrukturertAdresse strukturertAdresse = xmlMidlPostAdrNorge.getStrukturertAdresse();
    			if(strukturertAdresse instanceof XMLGateadresse) {
    				XMLGateadresse xmlGateAdresse = (XMLGateadresse) strukturertAdresse;
    				
    				String gatenummerString = getHusnummer(xmlGateAdresse);
    				String husbokstavString = getHusbokstav(xmlGateAdresse);
    				String postnummerString = getPostnummerString(xmlGateAdresse);
    				
    				Adresse midlertidigAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE);
    				midlertidigAdresse.setGyldigfra(gyldigFra);
    				midlertidigAdresse.setGyldigtil(gyldigTil);
    				midlertidigAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
    				midlertidigAdresse.setHusnummer(gatenummerString);
    				midlertidigAdresse.setHusbokstav(husbokstavString);
    				midlertidigAdresse.setPostnummer(postnummerString);
    		
    				result.add(midlertidigAdresse);
    			} else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
    				XMLPostboksadresseNorsk xmlPostboksAdresse = (XMLPostboksadresseNorsk) strukturertAdresse;
    				    				
    				Adresse midlertidigPostboksAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE);
    				midlertidigPostboksAdresse.setGyldigfra(gyldigFra);
    				midlertidigPostboksAdresse.setGyldigtil(gyldigTil);
    				
    				midlertidigPostboksAdresse.setAdresseeier(xmlPostboksAdresse.getTilleggsadresse());
    				midlertidigPostboksAdresse.setPostnummer(getPostnummerString(xmlPostboksAdresse));
    				midlertidigPostboksAdresse.setPostboksnavn(xmlPostboksAdresse.getPostboksanlegg());
    				midlertidigPostboksAdresse.setPostboksnummer(xmlPostboksAdresse.getPostboksnummer());
    				
    				result.add(midlertidigPostboksAdresse);
    				
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
	private String getPostnummerString(XMLPostboksadresseNorsk xmlPostboksAdresse) {
		XMLPostnummer postnummer = xmlPostboksAdresse.getPoststed();
		if(postnummer != null) {
			return  postnummer.getValue();
		}
		return "";
	}

	private String getHusnummer(XMLGateadresse xmlGateAdresse) {
		BigInteger gatenummer = xmlGateAdresse.getHusnummer();
		if(gatenummer != null) {
			return gatenummer.toString();
		}
		return "";
	}

	private String getHusbokstav(XMLGateadresse xmlGateAdresse) {
		String husbokstav = xmlGateAdresse.getHusbokstav();
		if(husbokstav != null) {
			return husbokstav;
		}
		return "";
	}
	
	private String finnFnr(XMLBruker soapPerson) {
    	return soapPerson.getIdent().getIdent();
	}

	private String finnForNavn(XMLBruker soapPerson) {
		if(soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null) {
			return soapPerson.getPersonnavn().getFornavn();
		} else {
			return "";
		}
    }

	private String finnMellomNavn(XMLBruker soapPerson) {
		if(soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null) {
			return soapPerson.getPersonnavn().getMellomnavn();
		} else {
			return "";
		}
	}

	private String finnEtterNavn(XMLBruker soapPerson) {
    	if(soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null) {
			return soapPerson.getPersonnavn().getEtternavn();
		} else {
			return "";
		}
	}

}
