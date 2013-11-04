package no.nav.sbl.dialogarena.person;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
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

        return new Person(soknadId, finnFnr(soapPerson), finnForNavn(soapPerson), finnMellomNavn(soapPerson), finnEtterNavn(soapPerson),
        		finnGjeldendeAdressetype(soapPerson), finnAdresser(soknadId, soapPerson));
    }

	private String finnGjeldendeAdressetype(XMLBruker soapPerson) {
		if(soapPerson.getGjeldendePostadresseType() != null) {
			return soapPerson.getGjeldendePostadresseType().getValue();
		}
		return "";
	}

	private List<Adresse> finnAdresser(long soknadId, XMLBruker soapPerson) {
		List<Adresse> result = new ArrayList<Adresse>();
    	XMLBostedsadresse bostedsadresse = soapPerson.getBostedsadresse();
    	if (bostedsadresse != null) {
			XMLStrukturertAdresse strukturertAdresse = bostedsadresse.getStrukturertAdresse();
			
			if(strukturertAdresse instanceof XMLGateadresse) {
				XMLGateadresse xmlGateAdresse = (XMLGateadresse)strukturertAdresse;
				Adresse personAdresse = hentBostedsAdresse(soknadId, xmlGateAdresse);
				result.add(personAdresse);
			}
    	}
    	XMLMidlertidigPostadresse midlertidigPostadresse = soapPerson.getMidlertidigPostadresse();
    	if(midlertidigPostadresse != null) {
    		if(midlertidigPostadresse instanceof XMLMidlertidigPostadresseNorge) {
    			XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
    			
    			DateTime gyldigFra = null;
    			DateTime gyldigTil = null;
    			XMLGyldighetsperiode postleveringsPeriode = xmlMidlPostAdrNorge.getPostleveringsPeriode();
    			if(postleveringsPeriode != null) {
    				gyldigFra = postleveringsPeriode.getFom();
    				gyldigTil = postleveringsPeriode.getTom();
    			}
    			
    			XMLStrukturertAdresse strukturertAdresse = xmlMidlPostAdrNorge.getStrukturertAdresse();
    			if(strukturertAdresse instanceof XMLGateadresse) {
    				XMLGateadresse xmlGateAdresse = (XMLGateadresse) strukturertAdresse;
    				
    				Adresse midlertidigAdresse = getMidlertidigPostadresseNorge(soknadId, gyldigFra, gyldigTil, xmlGateAdresse);
    				result.add(midlertidigAdresse);
    			} else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
    				XMLPostboksadresseNorsk xmlPostboksAdresse = (XMLPostboksadresseNorsk) strukturertAdresse;
    				    				
    				Adresse midlertidigPostboksAdresse = getMidlertidigPostboksadresseNorge(
							soknadId, gyldigFra, gyldigTil, xmlPostboksAdresse);
    				result.add(midlertidigPostboksAdresse);
    				
    			}
    		}
    		if(midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
    			Adresse midlertidigAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND);
    			XMLMidlertidigPostadresseUtland xmlMidlAdrUtland = (XMLMidlertidigPostadresseUtland) midlertidigPostadresse;
    			
    			getMidlertidigPostadresseUtland(midlertidigAdresse,	xmlMidlAdrUtland);
    			result.add(midlertidigAdresse);
    		}
    	}
    	XMLPostadresse postadresse = soapPerson.getPostadresse();
        finnPostAdresse(soknadId, result, postadresse);
        return result;
	}

    private void finnPostAdresse(long soknadId, List<Adresse> result, XMLPostadresse postadresse) {
        if(postadresse != null) {
            if(postadresse instanceof XMLPostadresse) {
                XMLPostadresse xmlPostadresse = postadresse;
                XMLUstrukturertAdresse ustrukturertAdresse = xmlPostadresse.getUstrukturertAdresse();
                if(ustrukturertAdresse != null) {
                    ArrayList<String> adresselinjer = hentAdresseLinjer(ustrukturertAdresse);

                    Adresse folkeregistrertUtenlandskAdresse = new Adresse(soknadId, Adressetype.UTENLANDSK_ADRESSE);

                    folkeregistrertUtenlandskAdresse.setUtenlandsadresse(adresselinjer);
                    XMLLandkoder xmlLandkode = ustrukturertAdresse.getLandkode();
                    if(xmlLandkode != null) {
                        String landkode = xmlLandkode.getValue();
                        folkeregistrertUtenlandskAdresse.setLand(kodeverk.getLand(landkode));
                    }
                    result.add(folkeregistrertUtenlandskAdresse);
                }
            }
        }
    }

    private void getMidlertidigPostadresseUtland(Adresse midlertidigAdresse,
			XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
		XMLUstrukturertAdresse ustrukturertAdresse = xmlMidlAdrUtland.getUstrukturertAdresse();
		
		if (ustrukturertAdresse != null) {
			ArrayList<String> adresselinjer = hentAdresseLinjer(ustrukturertAdresse);
			
			midlertidigAdresse.setUtenlandsadresse(adresselinjer);
			XMLLandkoder xmlLandkode = ustrukturertAdresse.getLandkode();
			if(xmlLandkode != null) {
				String landkode = xmlLandkode.getValue();
				midlertidigAdresse.setLand(kodeverk.getLand(landkode));
			}
		}
		DateTime gyldigFra = null;
		DateTime gyldigTil = null;
		XMLGyldighetsperiode postleveringsPeriode = xmlMidlAdrUtland.getPostleveringsPeriode();
		if(postleveringsPeriode != null) {
			gyldigFra = postleveringsPeriode.getFom();
			gyldigTil = postleveringsPeriode.getTom();
		}
		midlertidigAdresse.setGyldigfra(gyldigFra);
		midlertidigAdresse.setGyldigtil(gyldigTil);
	}

	private ArrayList<String> hentAdresseLinjer(
			XMLUstrukturertAdresse ustrukturertAdresse) {
		ArrayList<String> adresselinjer = new ArrayList<String>();
		if(ustrukturertAdresse.getAdresselinje1() != null) {
			adresselinjer.add(ustrukturertAdresse.getAdresselinje1());
		}
		if(ustrukturertAdresse.getAdresselinje2() != null) {
			adresselinjer.add(ustrukturertAdresse.getAdresselinje2());
		}
		if(ustrukturertAdresse.getAdresselinje3() != null) {
			adresselinjer.add(ustrukturertAdresse.getAdresselinje3());
		}
		if(ustrukturertAdresse.getAdresselinje4() != null) {
			adresselinjer.add(ustrukturertAdresse.getAdresselinje4());
		}
		return adresselinjer;
	}

	private Adresse getMidlertidigPostboksadresseNorge(long soknadId,
			DateTime gyldigFra, DateTime gyldigTil,
			XMLPostboksadresseNorsk xmlPostboksAdresse) {
		Adresse midlertidigPostboksAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE);
		midlertidigPostboksAdresse.setGyldigfra(gyldigFra);
		midlertidigPostboksAdresse.setGyldigtil(gyldigTil);
		
		midlertidigPostboksAdresse.setAdresseeier(xmlPostboksAdresse.getTilleggsadresse());
		midlertidigPostboksAdresse.setPostnummer(getPostnummerString(xmlPostboksAdresse));
		midlertidigPostboksAdresse.setPostboksnavn(xmlPostboksAdresse.getPostboksanlegg());
		midlertidigPostboksAdresse.setPostboksnummer(xmlPostboksAdresse.getPostboksnummer());
		return midlertidigPostboksAdresse;
	}

	private Adresse getMidlertidigPostadresseNorge(long soknadId,
			DateTime gyldigFra, DateTime gyldigTil,
			XMLGateadresse xmlGateAdresse) {
		String gatenummerString = getHusnummer(xmlGateAdresse);
		String husbokstavString = getHusbokstav(xmlGateAdresse);
		String postnummerString = getPostnummerString(xmlGateAdresse);
		
		Adresse midlertidigAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE);
		midlertidigAdresse.setGyldigfra(gyldigFra);
		midlertidigAdresse.setGyldigtil(gyldigTil);
		midlertidigAdresse.setAdresseeier(xmlGateAdresse.getTilleggsadresse());
		midlertidigAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
		midlertidigAdresse.setHusnummer(gatenummerString);
		midlertidigAdresse.setHusbokstav(husbokstavString);
		midlertidigAdresse.setPostnummer(postnummerString);
		return midlertidigAdresse;
	}

	private Adresse hentBostedsAdresse(long soknadId,
			XMLGateadresse xmlGateAdresse) {
		String gatenummerString = getHusnummer(xmlGateAdresse);
		String husbokstavString = getHusbokstav(xmlGateAdresse);
		
		String postnummerString = getPostnummerString(xmlGateAdresse);
		String poststed = kodeverk.getPoststed(postnummerString);
		Adresse personAdresse = new Adresse(soknadId, Adressetype.BOSTEDSADRESSE);
		personAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
		personAdresse.setHusnummer(gatenummerString);
		personAdresse.setHusbokstav(husbokstavString);
		personAdresse.setPostnummer(postnummerString);
		personAdresse.setPoststed(poststed);
		personAdresse.setLand(getLandkode(xmlGateAdresse));
		return personAdresse;
	}

	private String getLandkode(XMLGateadresse xmlGateAdresse) {
		if (xmlGateAdresse.getLandkode() != null) {
			return xmlGateAdresse.getLandkode().getValue(); 
		} else {
			return "";
		}
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
