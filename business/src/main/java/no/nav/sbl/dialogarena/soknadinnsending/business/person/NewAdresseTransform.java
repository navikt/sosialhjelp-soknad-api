package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import com.sun.deploy.util.StringUtils;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.BOSTEDSADRESSE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.POSTADRESSE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.UTENLANDSK_ADRESSE;
import static org.slf4j.LoggerFactory.getLogger;

public class NewAdresseTransform {
    private Kodeverk kodeverk;

    private static final Logger logger = getLogger(AdresseTransform.class);

    public NewAdresse mapGjeldendeAdresse(XMLBruker soapPerson, Kodeverk kodeverk) {
        this.kodeverk = kodeverk;

        if (harMidlertidigAdresseSomErGjeldendeAdresse(soapPerson)) {
            return finnMidlertidigAdresse(soapPerson.getMidlertidigPostadresse());
        } else if (harStrukturertAdresseSomErGjeldendeAdresse(soapPerson)) {
            return hentBostedsAdresse((XMLGateadresse) soapPerson.getBostedsadresse().getStrukturertAdresse());
        } else if (harUstrukturertAdresseSomErGjeldendeAdresse(soapPerson)) {
            return finnPostAdresse(soapPerson.getPostadresse());
        } else {
            return new NewAdresse();
        }
    }

    public NewAdresse mapSekundarAdresse(XMLBruker soapPerson, Kodeverk kodeverk) {
        this.kodeverk = kodeverk;

        if (harMidlertidigAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return finnMidlertidigAdresse(soapPerson.getMidlertidigPostadresse());
        } else if (harStrukturertAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return hentBostedsAdresse((XMLGateadresse) soapPerson.getBostedsadresse().getStrukturertAdresse());
        } else if (harUstrukturertAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return finnPostAdresse(soapPerson.getPostadresse());
        } else {
            return new NewAdresse();
        }
    }

    private boolean harMidlertidigAdresseSomErGjeldendeAdresse(XMLBruker soapPerson) {
        if (soapPerson.getMidlertidigPostadresse() != null) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (gjeldendeAdressetype.toLowerCase().startsWith("midlertidig")) {
                return true;
            }
        }
        return false;
    }

    private boolean harStrukturertAdresseSomErGjeldendeAdresse(XMLBruker soapPerson) {
        if (strukturertAdresseExists(soapPerson.getBostedsadresse())) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (gjeldendeAdressetype.equalsIgnoreCase(BOSTEDSADRESSE.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean harUstrukturertAdresseSomErGjeldendeAdresse(XMLBruker soapPerson) {
        if (ustrukturertAdresseExists(soapPerson.getPostadresse())) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (gjeldendeAdressetype.equalsIgnoreCase(UTENLANDSK_ADRESSE.name()) || gjeldendeAdressetype.equalsIgnoreCase(POSTADRESSE.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean harMidlertidigAdresseSomIkkeErGjeldendeAdresse(XMLBruker soapPerson) {
        if (soapPerson.getMidlertidigPostadresse() != null) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (!gjeldendeAdressetype.toLowerCase().startsWith("midlertidig")) {
                return true;
            }
        }
        return false;
    }

    private boolean harStrukturertAdresseSomIkkeErGjeldendeAdresse(XMLBruker soapPerson) {
        if (strukturertAdresseExists(soapPerson.getBostedsadresse())) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (!gjeldendeAdressetype.equalsIgnoreCase(BOSTEDSADRESSE.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean harUstrukturertAdresseSomIkkeErGjeldendeAdresse(XMLBruker soapPerson) {
        if (ustrukturertAdresseExists(soapPerson.getPostadresse())) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (!gjeldendeAdressetype.equalsIgnoreCase(UTENLANDSK_ADRESSE.name()) && !gjeldendeAdressetype.equalsIgnoreCase(POSTADRESSE.name())) {
                return true;
            }
        }
        return false;
    }

    private String finnGjeldendeAdressetype(XMLBruker soapPerson) {
        return soapPerson.getGjeldendePostadresseType() != null ? soapPerson.getGjeldendePostadresseType().getValue() : "";
    }

    private boolean strukturertAdresseExists(XMLBostedsadresse bostedsadresse) {
        return bostedsadresse != null && bostedsadresse.getStrukturertAdresse() instanceof XMLGateadresse;
    }

    private NewAdresse finnMidlertidigAdresse(XMLMidlertidigPostadresse midlertidigPostadresse) {
        if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseNorge) {
            XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
            return finnMidlertidigAdresse(xmlMidlPostAdrNorge, xmlMidlPostAdrNorge.getStrukturertAdresse());
        } else if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
            NewAdresse midlertidigAdresse = new NewAdresse();
            midlertidigAdresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_UTLAND.name());
            setLandkodeOgGyldighetsDatoer(midlertidigAdresse, (XMLMidlertidigPostadresseUtland) midlertidigPostadresse);
            return midlertidigAdresse;
        } else {
            return new NewAdresse();
        }
    }

    private NewAdresse finnMidlertidigAdresse(XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge, XMLStrukturertAdresse strukturertAdresse) {
        if (strukturertAdresse instanceof XMLGateadresse) {
            return getMidlertidigPostadresseNorge(
                    retrieveGyldigFra(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    retrieveGyldigTil(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    (XMLGateadresse) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
            return getMidlertidigPostboksadresseNorge(
                    retrieveGyldigFra(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    retrieveGyldigTil(xmlMidlPostAdrNorge.getPostleveringsPeriode()),
                    (XMLPostboksadresseNorsk) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLMatrikkeladresse) {
            return getMidlertidigOmrodeAdresse(
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

    private NewAdresse finnPostAdresse(XMLPostadresse postadresse) {
        XMLUstrukturertAdresse ustrukturertAdresse = postadresse.getUstrukturertAdresse();
        NewAdresse adresse = retrieveFolkeregistrertUtenlandskAdresse(ustrukturertAdresse);
        List<String> adresseLinjer = hentAdresseLinjer(ustrukturertAdresse);
        addIfNotNull(adresseLinjer, getLand(ustrukturertAdresse.getLandkode()));
        adresse.setAdresse(StringUtils.join(adresseLinjer, ", "));
        return adresse;
    }

    private boolean ustrukturertAdresseExists(XMLPostadresse postadresse) {
        return postadresse != null && postadresse.getUstrukturertAdresse() != null;
    }

    private String getLand(XMLLandkoder landkode) {
        if (landkode != null) {
            String land = kodeverk.getLand(landkode.getValue());
            if (land == null) {
                logger.warn("Kodeverk - Fant ikke land for kode: " + landkode.getValue() + " . Sjekk om kodeverk svarer.");
                return landkode.getValue();
            } else {
                return land;
            }
        }
        return "";
    }

    private NewAdresse retrieveFolkeregistrertUtenlandskAdresse(XMLUstrukturertAdresse ustrukturertAdresse) {
        NewAdresse adresse = new NewAdresse();
        if (ustrukturertAdresse.getLandkode() != null && "NOR".equals(ustrukturertAdresse.getLandkode().getValue())) {
            adresse.setAdressetype(POSTADRESSE.name());
        } else {
            adresse.setAdressetype(UTENLANDSK_ADRESSE.name());
        }
        return adresse;
    }

    private void setLandkodeOgGyldighetsDatoer(NewAdresse midlertidigAdresse, XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
        setLandkode(midlertidigAdresse, xmlMidlAdrUtland.getUstrukturertAdresse());
        setGyldigFraOgTil(midlertidigAdresse, xmlMidlAdrUtland.getPostleveringsPeriode());
    }

    private void setGyldigFraOgTil(NewAdresse midlertidigAdresse, XMLGyldighetsperiode postleveringsPeriode) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        midlertidigAdresse.setGyldigFra(postleveringsPeriode != null ? dateTimeFormat.print(postleveringsPeriode.getFom()) : "");
        midlertidigAdresse.setGyldigTil(postleveringsPeriode != null ? dateTimeFormat.print(postleveringsPeriode.getTom()) : "");
    }

    private void setLandkode(NewAdresse midlertidigAdresse, XMLUstrukturertAdresse ustrukturertAdresse) {
        if (ustrukturertAdresse != null) {
            String adresse = StringUtils.join(hentAdresseLinjer(ustrukturertAdresse), ", ");
            XMLLandkoder xmlLandkode = ustrukturertAdresse.getLandkode();
            if (xmlLandkode != null) {
                adresse += ", " + getLand(xmlLandkode);
            }
            midlertidigAdresse.setAdresse(adresse);
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

    private void addIfNotNull(List<String> adresselinjer, String adresselinje) {
        if (adresselinje != null) {
            adresselinjer.add(adresselinje);
        }
    }

    private NewAdresse getMidlertidigPostboksadresseNorge(DateTime gyldigFra, DateTime gyldigTil, XMLPostboksadresseNorsk xmlPostboksAdresse) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        NewAdresse adresse = new NewAdresse();
        adresse.setAdresse(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(dateTimeFormat.print(gyldigFra));
        adresse.setGyldigTil(dateTimeFormat.print(gyldigTil));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmlPostboksAdresse.getTilleggsadresse());
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlPostboksAdresse));
        stringBuilder.append(" ");
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlPostboksAdresse)));
        stringBuilder.append(", ");
        stringBuilder.append(xmlPostboksAdresse.getPostboksanlegg());
        stringBuilder.append(" ");
        stringBuilder.append(xmlPostboksAdresse.getPostboksnummer());
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
    }

    private NewAdresse getMidlertidigOmrodeAdresse(DateTime gyldigFra, DateTime gyldigTil, XMLMatrikkeladresse xmlMatrikkelAdresse) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        NewAdresse adresse = new NewAdresse();
        adresse.setAdresse(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(dateTimeFormat.print(gyldigFra));
        adresse.setGyldigTil(dateTimeFormat.print(gyldigTil));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmlMatrikkelAdresse.getTilleggsadresse());
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlMatrikkelAdresse));
        stringBuilder.append(" ");
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlMatrikkelAdresse)));
        stringBuilder.append(", ");
        stringBuilder.append(xmlMatrikkelAdresse.getEiendomsnavn());
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
    }

    private NewAdresse getMidlertidigPostadresseNorge(DateTime gyldigFra, DateTime gyldigTil, XMLGateadresse xmlGateAdresse) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
        NewAdresse adresse = new NewAdresse();
        adresse.setAdresse(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(dateTimeFormat.print(gyldigFra));
        adresse.setGyldigTil(dateTimeFormat.print(gyldigTil));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmlGateAdresse.getTilleggsadresse());
        stringBuilder.append(", ");
        stringBuilder.append(xmlGateAdresse.getGatenavn());
        stringBuilder.append(" ");
        stringBuilder.append(getHusnummer(xmlGateAdresse));
        stringBuilder.append(getHusbokstav(xmlGateAdresse));
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlGateAdresse));
        stringBuilder.append(" ");
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
        stringBuilder.append(", ");
        stringBuilder.append(getLandkode(xmlGateAdresse));
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
    }

    private NewAdresse hentBostedsAdresse(XMLGateadresse xmlGateAdresse) {
        NewAdresse adresse = new NewAdresse();
        adresse.setAdresse(BOSTEDSADRESSE.name());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmlGateAdresse.getGatenavn());
        stringBuilder.append(" ");
        stringBuilder.append(getHusnummer(xmlGateAdresse));
        stringBuilder.append(getHusbokstav(xmlGateAdresse));
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlGateAdresse));
        stringBuilder.append(" ");
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
        stringBuilder.append(", ");
        stringBuilder.append(getLand(xmlGateAdresse.getLandkode()));
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
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

}
