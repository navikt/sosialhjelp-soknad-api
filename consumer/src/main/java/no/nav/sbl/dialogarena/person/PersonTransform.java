package no.nav.sbl.dialogarena.person;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
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
import static no.nav.sbl.dialogarena.person.Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND;
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
        Person person = new Person(
                soknadId,
                finnFnr(soapPerson),
                finnForNavn(soapPerson),
                finnMellomNavn(soapPerson),
                finnEtterNavn(soapPerson),
                finnGjeldendeAdressetype(soapPerson),
                finnAdresser(soknadId, soapPerson));
        person.setEpost(soknadId,finnEpost(soapPerson));
        
        return person;
    }

    private String finnEpost(XMLBruker soapPerson) {
        for (XMLElektroniskKommunikasjonskanal kanal : soapPerson.getElektroniskKommunikasjonskanal()) {
            if (kanal.getElektroniskAdresse() instanceof XMLEPost) {
                return ((XMLEPost) kanal.getElektroniskAdresse()).getIdentifikator();
            }
        }
        return null;
    }

    private String finnGjeldendeAdressetype(XMLBruker soapPerson) {
        return soapPerson.getGjeldendePostadresseType() != null ? soapPerson.getGjeldendePostadresseType().getValue() : "";
    }

    private List<Adresse> finnAdresser(long soknadId, XMLBruker soapPerson) {
        List<Adresse> result = new ArrayList<>();
        if (strukturertAdresseExists(soapPerson.getBostedsadresse())) {
            result.add(hentBostedsAdresse(soknadId, (XMLGateadresse) soapPerson.getBostedsadresse().getStrukturertAdresse()));
        }
        if (soapPerson.getMidlertidigPostadresse() != null) {
            addMidlertidigAdresse(soknadId, result, soapPerson.getMidlertidigPostadresse());
        }
        finnPostAdresse(soknadId, result, soapPerson.getPostadresse());
        return result;
    }

    private boolean strukturertAdresseExists(XMLBostedsadresse bostedsadresse) {
        return bostedsadresse != null && bostedsadresse.getStrukturertAdresse() instanceof XMLGateadresse;
    }

    private void addMidlertidigAdresse(long soknadId, List<Adresse> result, XMLMidlertidigPostadresse midlertidigPostadresse) {
        if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseNorge) {
            XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
            result.add(retrieveAdresse(soknadId, xmlMidlPostAdrNorge, xmlMidlPostAdrNorge.getStrukturertAdresse()));
        }
        else if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
            Adresse midlertidigAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_UTLAND);
            setLandkodeOgGyldighetsDatoer(midlertidigAdresse, (XMLMidlertidigPostadresseUtland) midlertidigPostadresse);
            result.add(midlertidigAdresse);
        }
    }

    private Adresse retrieveAdresse(long soknadId, XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge, XMLStrukturertAdresse strukturertAdresse) {
        if (strukturertAdresse instanceof XMLGateadresse) {
            return getMidlertidigPostadresseNorge(
                    soknadId,
                    retrieveGyldigFra(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    retrieveGyldigTil(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    (XMLGateadresse) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
            return getMidlertidigPostboksadresseNorge(
                    soknadId,
                    retrieveGyldigFra(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    retrieveGyldigTil(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    (XMLPostboksadresseNorsk) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLMatrikkeladresse) {
            return getMidlertidigOmrodeAdresse(
                    soknadId,
                    retrieveGyldigFra(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    retrieveGyldigTil(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    (XMLMatrikkeladresse) strukturertAdresse);
        }
        throw new ApplicationException("ukjent adressetype med klassenavn: " + strukturertAdresse.getClass().getCanonicalName());
    }

    private DateTime retrieveGyldigTil(XMLGyldighetsperiode postleveringsPeriode) {
        return postleveringsPeriode != null ? postleveringsPeriode.getTom() : null;
    }

    private DateTime retrieveGyldigFra(XMLGyldighetsperiode postleveringsPeriode) {
        return postleveringsPeriode != null ? postleveringsPeriode.getFom() : null;
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

    private void setLandkodeOgGyldighetsDatoer(Adresse midlertidigAdresse, XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
        setLandkode(midlertidigAdresse, xmlMidlAdrUtland.getUstrukturertAdresse());
        setGyldigFraOgTil(midlertidigAdresse, xmlMidlAdrUtland.getPostleveringsPeriode());
    }

    private void setGyldigFraOgTil(Adresse midlertidigAdresse, XMLGyldighetsperiode postleveringsPeriode) {
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
        Adresse midlertidigPostboksAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigPostboksAdresse.setGyldigfra(gyldigFra);
        midlertidigPostboksAdresse.setGyldigtil(gyldigTil);
        midlertidigPostboksAdresse.setAdresseeier(xmlPostboksAdresse.getTilleggsadresse());
        midlertidigPostboksAdresse.setPostnummer(getPostnummerString(xmlPostboksAdresse));
        midlertidigPostboksAdresse.setPoststed(kodeverk.getPoststed(getPostnummerString(xmlPostboksAdresse)));
        midlertidigPostboksAdresse.setPostboksnavn(xmlPostboksAdresse.getPostboksanlegg());
        midlertidigPostboksAdresse.setPostboksnummer(xmlPostboksAdresse.getPostboksnummer());
        return midlertidigPostboksAdresse;
    }

    private Adresse getMidlertidigOmrodeAdresse(long soknadId, DateTime gyldigFra, DateTime gyldigTil, XMLMatrikkeladresse xmlMatrikkelAdresse) {
        Adresse midlertidigOmrodeAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigOmrodeAdresse.setGyldigfra(gyldigFra);
        midlertidigOmrodeAdresse.setGyldigtil(gyldigTil);
        midlertidigOmrodeAdresse.setAdresseeier(xmlMatrikkelAdresse.getTilleggsadresse());
        midlertidigOmrodeAdresse.setPostnummer(getPostnummerString(xmlMatrikkelAdresse));
        midlertidigOmrodeAdresse.setPoststed(kodeverk.getPoststed(getPostnummerString(xmlMatrikkelAdresse)));
        midlertidigOmrodeAdresse.setEiendomsnavn(xmlMatrikkelAdresse.getEiendomsnavn());
        return midlertidigOmrodeAdresse;
    }

    private Adresse getMidlertidigPostadresseNorge(long soknadId, DateTime gyldigFra, DateTime gyldigTil, XMLGateadresse xmlGateAdresse) {
        Adresse midlertidigAdresse = new Adresse(soknadId, MIDLERTIDIG_POSTADRESSE_NORGE);
        midlertidigAdresse.setGyldigfra(gyldigFra);
        midlertidigAdresse.setGyldigtil(gyldigTil);
        midlertidigAdresse.setAdresseeier(xmlGateAdresse.getTilleggsadresse());
        midlertidigAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
        midlertidigAdresse.setHusnummer(getHusnummer(xmlGateAdresse));
        midlertidigAdresse.setHusbokstav(getHusbokstav(xmlGateAdresse));
        midlertidigAdresse.setPostnummer(getPostnummerString(xmlGateAdresse));
        midlertidigAdresse.setPoststed(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
        midlertidigAdresse.setLand(getLandkode(xmlGateAdresse));
        return midlertidigAdresse;
    }

    private Adresse hentBostedsAdresse(long soknadId, XMLGateadresse xmlGateAdresse) {
        Adresse personAdresse = new Adresse(soknadId, Adressetype.BOSTEDSADRESSE);
        personAdresse.setGatenavn(xmlGateAdresse.getGatenavn());
        personAdresse.setHusnummer(getHusnummer(xmlGateAdresse));
        personAdresse.setHusbokstav(getHusbokstav(xmlGateAdresse));
        personAdresse.setPostnummer(getPostnummerString(xmlGateAdresse));
        personAdresse.setPoststed(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
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
