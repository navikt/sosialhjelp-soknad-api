package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMatrikkeladresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.person.Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.person.Adressetype.POSTADRESSE;
import static no.nav.sbl.dialogarena.person.Adressetype.UTENLANDSK_ADRESSE;

/**
 * Map from TPS data format to internal domain model
 */
public class PersonTransform {

    private Kodeverk kodeverk;

    public Person mapToPerson(Long soknadId, XMLHentKontaktinformasjonOgPreferanserResponse response, Kodeverk kodeverk) {
        this.kodeverk = kodeverk;
        if (response == null) {
            return new Person();
        }
        XMLBruker soapPerson = (XMLBruker) response.getPerson();
        return new Person(
                soknadId,
                finnFnr(soapPerson),
                finnForNavn(soapPerson),
                finnMellomNavn(soapPerson),
                finnEtterNavn(soapPerson),
                finnEpost(soapPerson),
                finnGjeldendeAdressetype(soapPerson),
                finnAdresser(soknadId, soapPerson));
    }

    private String finnEpost(XMLBruker soapPerson) {
        String epost = null;
        List<XMLElektroniskKommunikasjonskanal> elkanaler = soapPerson.getElektroniskKommunikasjonskanal();
        for (XMLElektroniskKommunikasjonskanal kanal : elkanaler) {
            XMLElektroniskAdresse adr = kanal.getElektroniskAdresse();
            if (adr instanceof XMLEPost) {
                epost = ((XMLEPost) adr).getIdentifikator();
            }
        }
        return epost;
    }

    private String finnGjeldendeAdressetype(XMLBruker soapPerson) {
        if (soapPerson.getGjeldendePostadresseType() != null) {
            return soapPerson.getGjeldendePostadresseType().getValue();
        }
        return "";
    }

    private List<Adresse> finnAdresser(long soknadId, XMLBruker soapPerson) {
        List<Adresse> result = new ArrayList<Adresse>();
        XMLBostedsadresse bostedsadresse = soapPerson.getBostedsadresse();
        if (bostedsadresse != null) {
            XMLStrukturertAdresse strukturertAdresse = bostedsadresse.getStrukturertAdresse();
            if (strukturertAdresse instanceof XMLGateadresse) {
                XMLGateadresse xmlGateAdresse = (XMLGateadresse) strukturertAdresse;
                Adresse personAdresse = hentBostedsAdresse(soknadId, xmlGateAdresse);
                result.add(personAdresse);
            }
        }
        XMLMidlertidigPostadresse midlertidigPostadresse = soapPerson.getMidlertidigPostadresse();
        if (midlertidigPostadresse != null) {
            if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseNorge) {
                XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
                DateTime gyldigFra = null;
                DateTime gyldigTil = null;
                XMLGyldighetsperiode postleveringsPeriode = xmlMidlPostAdrNorge.getPostleveringsPeriode();
                if (postleveringsPeriode != null) {
                    gyldigFra = postleveringsPeriode.getFom();
                    gyldigTil = postleveringsPeriode.getTom();
                }
                XMLStrukturertAdresse strukturertAdresse = xmlMidlPostAdrNorge.getStrukturertAdresse();
                if (strukturertAdresse instanceof XMLGateadresse) {
                    XMLGateadresse xmlGateAdresse = (XMLGateadresse) strukturertAdresse;

                    Adresse midlertidigAdresse = getMidlertidigPostadresseNorge(soknadId, gyldigFra, gyldigTil, xmlGateAdresse);
                    result.add(midlertidigAdresse);
                } else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
                    XMLPostboksadresseNorsk xmlPostboksAdresse = (XMLPostboksadresseNorsk) strukturertAdresse;
                    Adresse midlertidigPostboksAdresse = getMidlertidigPostboksadresseNorge(soknadId, gyldigFra, gyldigTil, xmlPostboksAdresse);
                    result.add(midlertidigPostboksAdresse);
                } else if (strukturertAdresse instanceof XMLMatrikkeladresse) {
                    XMLMatrikkeladresse xmlMatrikkelAdresse = (XMLMatrikkeladresse) strukturertAdresse;
                    Adresse midlertidigOmrodeAdresse = getMidlertidigOmrodeAdresse(soknadId, gyldigFra, gyldigTil, xmlMatrikkelAdresse);
                    result.add(midlertidigOmrodeAdresse);
                }
            }
            if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
                Adresse midlertidigAdresse = new Adresse(soknadId, Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND);
                XMLMidlertidigPostadresseUtland xmlMidlAdrUtland = (XMLMidlertidigPostadresseUtland) midlertidigPostadresse;
                getMidlertidigPostadresseUtland(midlertidigAdresse, xmlMidlAdrUtland);
                result.add(midlertidigAdresse);
            }
        }
        XMLPostadresse postadresse = soapPerson.getPostadresse();
        finnPostAdresse(soknadId, result, postadresse);
        return result;
    }

    private void finnPostAdresse(long soknadId, List<Adresse> result, XMLPostadresse postadresse) {
        if (ustrukturertAdresseExists(postadresse)) {
            XMLUstrukturertAdresse ustrukturertAdresse = postadresse.getUstrukturertAdresse();
            Adresse folkeregistrertUtenlandskAdresse = retrieveFolkeregistrertUtenlandskAdresse(soknadId, ustrukturertAdresse);
            folkeregistrertUtenlandskAdresse.setAdresselinjer(hentAdresseLinjer(ustrukturertAdresse));
            folkeregistrertUtenlandskAdresse.setLand(getLand(ustrukturertAdresse.getLandkode()));
            result.add(folkeregistrertUtenlandskAdresse);
        }
    }

    private boolean ustrukturertAdresseExists(XMLPostadresse postadresse) {
        return postadresse != null && postadresse.getUstrukturertAdresse() != null;
    }

    private String getLand(XMLLandkoder landkode) {
        return landkode != null ? kodeverk.getLand(landkode.getValue()) : null;
    }

    private Adresse retrieveFolkeregistrertUtenlandskAdresse(long soknadId, XMLUstrukturertAdresse ustrukturertAdresse) {
        if (ustrukturertAdresse.getLandkode() != null && "NOR".equals(ustrukturertAdresse.getLandkode().getValue())) {
            return new Adresse(soknadId, POSTADRESSE);
        } else {
            return new Adresse(soknadId, UTENLANDSK_ADRESSE);
        }
    }

    private void getMidlertidigPostadresseUtland(Adresse midlertidigAdresse, XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
        setLandkode(midlertidigAdresse, xmlMidlAdrUtland.getUstrukturertAdresse());
        setGyldigFraOgTil(midlertidigAdresse, xmlMidlAdrUtland);
    }

    private void setGyldigFraOgTil(Adresse midlertidigAdresse, XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
        XMLGyldighetsperiode postleveringsPeriode = xmlMidlAdrUtland.getPostleveringsPeriode();
        midlertidigAdresse.setGyldigfra(postleveringsPeriode != null ? postleveringsPeriode.getFom() : null);
        midlertidigAdresse.setGyldigtil(postleveringsPeriode != null ? postleveringsPeriode.getTom() : null);
    }

    private void setLandkode(Adresse midlertidigAdresse, XMLUstrukturertAdresse ustrukturertAdresse) {
        if (ustrukturertAdresse != null) {
            midlertidigAdresse.setAdresselinjer(hentAdresseLinjer(ustrukturertAdresse));
            XMLLandkoder xmlLandkode = ustrukturertAdresse.getLandkode();
            if (xmlLandkode != null) {
                midlertidigAdresse.setLand(kodeverk.getLand(xmlLandkode.getValue()));
            }
        }
    }

    private List<String> hentAdresseLinjer(XMLUstrukturertAdresse ustrukturertAdresse) {
        ArrayList<String> adresselinjer = new ArrayList<>();
        addIfNotNull(adresselinjer, ustrukturertAdresse.getAdresselinje1());
        addIfNotNull(adresselinjer, ustrukturertAdresse.getAdresselinje2());
        addIfNotNull(adresselinjer, ustrukturertAdresse.getAdresselinje3());
        addIfNotNull(adresselinjer, ustrukturertAdresse.getAdresselinje4());
        return adresselinjer;
    }

    private void addIfNotNull(ArrayList<String> adresselinjer, String adresselinje) {
        if (adresselinje != null) {
            adresselinjer.add(adresselinje);
        }
    }

    private Adresse getMidlertidigPostboksadresseNorge(long soknadId, DateTime gyldigFra, DateTime gyldigTil, XMLPostboksadresseNorsk xmlPostboksAdresse) {
        String postnummerString = getPostnummerString(xmlPostboksAdresse);
        Adresse midlertidigPostboksAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigPostboksAdresse.setGyldigfra(gyldigFra);
        midlertidigPostboksAdresse.setGyldigtil(gyldigTil);
        midlertidigPostboksAdresse.setAdresseeier(xmlPostboksAdresse.getTilleggsadresse());
        midlertidigPostboksAdresse.setPostnummer(postnummerString);
        midlertidigPostboksAdresse.setPoststed(kodeverk.getPoststed(postnummerString));
        midlertidigPostboksAdresse.setPostboksnavn(xmlPostboksAdresse.getPostboksanlegg());
        midlertidigPostboksAdresse.setPostboksnummer(xmlPostboksAdresse.getPostboksnummer());
        return midlertidigPostboksAdresse;
    }

    private Adresse getMidlertidigOmrodeAdresse(long soknadId, DateTime gyldigFra, DateTime gyldigTil, XMLMatrikkeladresse xmlMatrikkelAdresse) {
        String postnummerString = getPostnummerString(xmlMatrikkelAdresse);
        Adresse midlertidigOmrodeAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigOmrodeAdresse.setGyldigfra(gyldigFra);
        midlertidigOmrodeAdresse.setGyldigtil(gyldigTil);
        midlertidigOmrodeAdresse.setAdresseeier(xmlMatrikkelAdresse.getTilleggsadresse());
        midlertidigOmrodeAdresse.setPostnummer(postnummerString);
        midlertidigOmrodeAdresse.setPoststed(kodeverk.getPoststed(postnummerString));
        midlertidigOmrodeAdresse.setEiendomsnavn(xmlMatrikkelAdresse.getEiendomsnavn());
        return midlertidigOmrodeAdresse;
    }

    private Adresse getMidlertidigPostadresseNorge(long soknadId, DateTime gyldigFra, DateTime gyldigTil, XMLGateadresse xmlGateAdresse) {
        String postnummerString = getPostnummerString(xmlGateAdresse);
        Adresse midlertidigAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigAdresse.setGyldigfra(gyldigFra);
        midlertidigAdresse.setGyldigtil(gyldigTil);
        midlertidigAdresse.setAdresseeier(xmlGateAdresse.getTilleggsadresse());
        midlertidigAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
        midlertidigAdresse.setHusnummer(getHusnummer(xmlGateAdresse));
        midlertidigAdresse.setHusbokstav(getHusbokstav(xmlGateAdresse));
        midlertidigAdresse.setPostnummer(postnummerString);
        midlertidigAdresse.setPoststed(kodeverk.getPoststed(postnummerString));
        midlertidigAdresse.setLand(getLandkode(xmlGateAdresse));
        return midlertidigAdresse;
    }

    private Adresse hentBostedsAdresse(long soknadId, XMLGateadresse xmlGateAdresse) {
        String postnummerString = getPostnummerString(xmlGateAdresse);
        Adresse personAdresse = new Adresse(soknadId, Adressetype.BOSTEDSADRESSE);
        personAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
        personAdresse.setHusnummer(getHusnummer(xmlGateAdresse));
        personAdresse.setHusbokstav(getHusbokstav(xmlGateAdresse));
        personAdresse.setPostnummer(postnummerString);
        personAdresse.setPoststed(kodeverk.getPoststed(postnummerString));
        personAdresse.setLand(getLandkode(xmlGateAdresse));
        return personAdresse;
    }

    private String getLandkode(XMLGateadresse xmlGateAdresse) {
        return xmlGateAdresse.getLandkode() != null ? xmlGateAdresse.getLandkode().getValue() : "";
    }

    private String getPostnummerString(XMLGateadresse xmlGateAdresse) {
        return xmlGateAdresse.getPoststed() != null ? xmlGateAdresse.getPoststed().getValue() : "";
    }

    private String getPostnummerString(XMLPostboksadresseNorsk xmlPostboksAdresse) {
        return xmlPostboksAdresse.getPoststed() != null ? xmlPostboksAdresse.getPoststed().getValue() : "";
    }

    private String getPostnummerString(XMLMatrikkeladresse xmlMatrikkelAdresse) {
        return xmlMatrikkelAdresse.getPoststed() != null ? xmlMatrikkelAdresse.getPoststed().getValue() : "";
    }

    private String getHusnummer(XMLGateadresse xmlGateAdresse) {
        return xmlGateAdresse.getHusnummer() != null ? xmlGateAdresse.getHusnummer().toString() : "";
    }

    private String getHusbokstav(XMLGateadresse xmlGateAdresse) {
        return xmlGateAdresse.getHusbokstav() != null ? xmlGateAdresse.getHusbokstav() : "";
    }

    private String finnFnr(XMLBruker soapPerson) {
        return soapPerson.getIdent().getIdent();
    }

    private String finnForNavn(XMLBruker soapPerson) {
        return fornavnExists(soapPerson) ? soapPerson.getPersonnavn().getFornavn() : "";
    }

    private boolean fornavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getFornavn() != null;
    }

    private String finnMellomNavn(XMLBruker soapPerson) {
        return mellomnavnExists(soapPerson) ? soapPerson.getPersonnavn().getMellomnavn() : "";
    }

    private boolean mellomnavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getMellomnavn() != null;
    }

    private String finnEtterNavn(XMLBruker soapPerson) {
        return etternavnExists(soapPerson) ? soapPerson.getPersonnavn().getEtternavn() : "";
    }

    private boolean etternavnExists(XMLBruker soapPerson) {
        return soapPerson.getPersonnavn() != null && soapPerson.getPersonnavn().getEtternavn() != null;
    }

}
