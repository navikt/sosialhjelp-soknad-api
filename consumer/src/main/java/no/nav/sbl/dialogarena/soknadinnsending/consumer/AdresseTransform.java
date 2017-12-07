package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype.*;
import static org.slf4j.LoggerFactory.getLogger;

public class AdresseTransform {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final String C_O = "C/O";
    private static final String MIDLERTIDIG = "midlertidig";
    private Kodeverk kodeverk;

    private static final Logger logger = getLogger(AdresseTransform.class);
    private static final String NORGE = "NOR";

    public Adresse mapGjeldendeAdresse(XMLBruker soapPerson, Kodeverk kodeverk) {
        this.kodeverk = kodeverk;

        if (harHemmeligAdresse(soapPerson)) {
            return new Adresse();
        } else if (harMidlertidigAdresseSomErGjeldendeAdresse(soapPerson)) {
            return finnMidlertidigAdresse(soapPerson.getMidlertidigPostadresse());
        } else if (harStrukturertAdresseSomErGjeldendeAdresse(soapPerson)) {
            return hentBostedsAdresse((XMLGateadresse) soapPerson.getBostedsadresse().getStrukturertAdresse());
        } else if (harUstrukturertAdresseSomErGjeldendeAdresse(soapPerson)) {
            return finnPostAdresse(soapPerson.getPostadresse());
        } else {
            return new Adresse();
        }
    }

    private static final List<String> HEMMELIGE_DISKRESJONSKODER = Arrays.asList("6", "7");

    private boolean harHemmeligAdresse(XMLBruker soapPerson) {
        return soapPerson.getDiskresjonskode() != null && HEMMELIGE_DISKRESJONSKODER.contains(soapPerson.getDiskresjonskode().getValue());
    }

    public Adresse mapSekundarAdresse(XMLBruker soapPerson, Kodeverk kodeverk) {
        this.kodeverk = kodeverk;

        if (harHemmeligAdresse(soapPerson)) {
            return new Adresse();
        } else if (harMidlertidigAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return finnMidlertidigAdresse(soapPerson.getMidlertidigPostadresse());
        } else if (harStrukturertAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return hentBostedsAdresse((XMLGateadresse) soapPerson.getBostedsadresse().getStrukturertAdresse());
        } else if (harUstrukturertAdresseSomIkkeErGjeldendeAdresse(soapPerson)) {
            return finnPostAdresse(soapPerson.getPostadresse());
        } else {
            return new Adresse();
        }
    }

    private boolean harMidlertidigAdresseSomErGjeldendeAdresse(XMLBruker soapPerson) {
        if (soapPerson.getMidlertidigPostadresse() != null) {
            String gjeldendeAdressetype = finnGjeldendeAdressetype(soapPerson);
            if (gjeldendeAdressetype.toLowerCase().startsWith(MIDLERTIDIG)) {
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
            if (!gjeldendeAdressetype.toLowerCase().startsWith(MIDLERTIDIG)) {
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

    private Adresse finnMidlertidigAdresse(XMLMidlertidigPostadresse midlertidigPostadresse) {
        if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseNorge) {
            XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge = (XMLMidlertidigPostadresseNorge) midlertidigPostadresse;
            return finnMidlertidigAdresse(xmlMidlPostAdrNorge, xmlMidlPostAdrNorge.getStrukturertAdresse());
        } else if (midlertidigPostadresse instanceof XMLMidlertidigPostadresseUtland) {
            Adresse midlertidigAdresse = new Adresse();
            midlertidigAdresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_UTLAND.name());
            setLandkodeOgGyldighetsDatoer(midlertidigAdresse, (XMLMidlertidigPostadresseUtland) midlertidigPostadresse);
            return midlertidigAdresse;
        } else {
            return new Adresse();
        }
    }

    private Adresse finnMidlertidigAdresse(XMLMidlertidigPostadresseNorge xmlMidlPostAdrNorge, XMLStrukturertAdresse strukturertAdresse) {
        XMLGyldighetsperiode periode = xmlMidlPostAdrNorge.getPostleveringsPeriode();
        if (strukturertAdresse instanceof XMLGateadresse) {
            return getMidlertidigPostadresseNorge(
                    optional(periode).map(FRA_DATO).orNull(),
                    optional(periode).map(TIL_DATO).orNull(),
                    (XMLGateadresse) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLPostboksadresseNorsk) {
            return getMidlertidigPostboksadresseNorge(
                    optional(periode).map(FRA_DATO).orNull(),
                    optional(periode).map(TIL_DATO).orNull(),
                    (XMLPostboksadresseNorsk) strukturertAdresse);
        } else if (strukturertAdresse instanceof XMLMatrikkeladresse) {
            return getMidlertidigOmrodeAdresse(
                    optional(periode).map(FRA_DATO).orNull(),
                    optional(periode).map(TIL_DATO).orNull(),
                    (XMLMatrikkeladresse) strukturertAdresse);
        }
        throw new ApplicationException("ukjent adressetype med klassenavn: " + strukturertAdresse.getClass().getCanonicalName());
    }

    private static final Transformer<XMLGyldighetsperiode, DateTime> TIL_DATO = new Transformer<XMLGyldighetsperiode, DateTime>() {
        @Override
        public DateTime transform(XMLGyldighetsperiode xmlGyldighetsperiode) {
            return xmlGyldighetsperiode.getTom();
        }
    };

    private static final Transformer<XMLGyldighetsperiode, DateTime> FRA_DATO = new Transformer<XMLGyldighetsperiode, DateTime>() {
        @Override
        public DateTime transform(XMLGyldighetsperiode xmlGyldighetsperiode) {
            return xmlGyldighetsperiode.getFom();
        }
    };

    private Adresse finnPostAdresse(XMLPostadresse postadresse) {
        XMLUstrukturertAdresse ustrukturertAdresse = postadresse.getUstrukturertAdresse();
        Adresse adresse = retrieveFolkeregistrertUtenlandskAdresse(ustrukturertAdresse);
        adresse.setLandkode(ustrukturertAdresse.getLandkode().getValue());
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

    private Adresse retrieveFolkeregistrertUtenlandskAdresse(XMLUstrukturertAdresse ustrukturertAdresse) {
        Adresse adresse = new Adresse();
        adresse.setLandkode(ustrukturertAdresse.getLandkode().getValue());
        if (ustrukturertAdresse.getLandkode() != null && NORGE.equals(ustrukturertAdresse.getLandkode().getValue())) {
            adresse.setAdressetype(POSTADRESSE.name());
        } else {
            adresse.setAdressetype(UTENLANDSK_ADRESSE.name());
        }
        return adresse;
    }

    private void setLandkodeOgGyldighetsDatoer(Adresse midlertidigAdresse, XMLMidlertidigPostadresseUtland xmlMidlAdrUtland) {
        setLandkode(midlertidigAdresse, xmlMidlAdrUtland.getUstrukturertAdresse());
        setGyldigFraOgTil(midlertidigAdresse, xmlMidlAdrUtland.getPostleveringsPeriode());
    }

    private void setGyldigFraOgTil(Adresse midlertidigAdresse, XMLGyldighetsperiode postleveringsPeriode) {
        midlertidigAdresse.setGyldigFra(postleveringsPeriode != null ? DATE_TIME_FORMATTER.print(postleveringsPeriode.getFom()) : "");
        midlertidigAdresse.setGyldigTil(postleveringsPeriode != null ? DATE_TIME_FORMATTER.print(postleveringsPeriode.getTom()) : "");
    }

    private void setLandkode(Adresse midlertidigAdresse, XMLUstrukturertAdresse ustrukturertAdresse) {
        if (ustrukturertAdresse != null) {
            String adresse = StringUtils.join(hentAdresseLinjer(ustrukturertAdresse), ", ");
            XMLLandkoder xmlLandkode = ustrukturertAdresse.getLandkode();
            if (xmlLandkode != null) {
                adresse += ", " + getLand(xmlLandkode);
                midlertidigAdresse.setLandkode(xmlLandkode.getValue());
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

    private Adresse getMidlertidigPostboksadresseNorge(DateTime gyldigFra, DateTime gyldigTil, XMLPostboksadresseNorsk xmlPostboksAdresse) {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(DATE_TIME_FORMATTER.print(gyldigFra));
        adresse.setGyldigTil(DATE_TIME_FORMATTER.print(gyldigTil));

        StringBuilder stringBuilder = new StringBuilder();

        if (xmlPostboksAdresse.getTilleggsadresse() != null) {
            stringBuilder.append(C_O).append(' ').append(xmlPostboksAdresse.getTilleggsadresse()).append(", ");
        }

        String postboksanlegg = xmlPostboksAdresse.getPostboksanlegg();
        if (postboksanlegg != null && !postboksanlegg.isEmpty()) {
            stringBuilder.append("Postboks ").append(xmlPostboksAdresse.getPostboksnummer()).append(' ').append(postboksanlegg).append(", ");
        }

        stringBuilder.append(getPostnummerString(xmlPostboksAdresse));
        stringBuilder.append(' ');
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlPostboksAdresse)));

        adresse.setAdresse(stringBuilder.toString());
        return adresse;
    }

    private Adresse getMidlertidigOmrodeAdresse(DateTime gyldigFra, DateTime gyldigTil, XMLMatrikkeladresse xmlMatrikkelAdresse) {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(DATE_TIME_FORMATTER.print(gyldigFra));
        adresse.setGyldigTil(DATE_TIME_FORMATTER.print(gyldigTil));
        adresse.setStrukturertAdresse(tilMatrikkeladresse(xmlMatrikkelAdresse));

        StringBuilder stringBuilder = new StringBuilder();

        if (xmlMatrikkelAdresse.getTilleggsadresse() != null) {
            stringBuilder.append(C_O).append(' ').append(xmlMatrikkelAdresse.getTilleggsadresse()).append(", ");
        }

        if (xmlMatrikkelAdresse.getEiendomsnavn() != null) {
            stringBuilder.append(xmlMatrikkelAdresse.getEiendomsnavn()).append(", ");
        }

        stringBuilder.append(getPostnummerString(xmlMatrikkelAdresse));
        stringBuilder.append(' ');
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlMatrikkelAdresse)));

        adresse.setAdresse(stringBuilder.toString());
        return adresse;
    }

    private Adresse getMidlertidigPostadresseNorge(DateTime gyldigFra, DateTime gyldigTil, XMLGateadresse xmlGateAdresse) {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(MIDLERTIDIG_POSTADRESSE_NORGE.name());
        adresse.setGyldigFra(DATE_TIME_FORMATTER.print(gyldigFra));
        adresse.setGyldigTil(DATE_TIME_FORMATTER.print(gyldigTil));
        adresse.setStrukturertAdresse(tilGatedresse(xmlGateAdresse));

        StringBuilder stringBuilder = new StringBuilder();
        if (xmlGateAdresse.getTilleggsadresse() != null) {
            stringBuilder.append(C_O).append(' ').append(xmlGateAdresse.getTilleggsadresse()).append(", ");
        }
        stringBuilder.append(xmlGateAdresse.getGatenavn());
        stringBuilder.append(' ');
        stringBuilder.append(getHusnummer(xmlGateAdresse));
        stringBuilder.append(getHusbokstav(xmlGateAdresse));
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlGateAdresse));
        stringBuilder.append(' ');
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
    }

    private Adresse hentBostedsAdresse(XMLGateadresse xmlGateAdresse) {
        Adresse adresse = new Adresse();
        adresse.setAdressetype(BOSTEDSADRESSE.name());
        adresse.setStrukturertAdresse(tilGatedresse(xmlGateAdresse));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(xmlGateAdresse.getGatenavn());
        stringBuilder.append(' ');
        stringBuilder.append(getHusnummer(xmlGateAdresse));
        stringBuilder.append(getHusbokstav(xmlGateAdresse));
        stringBuilder.append(", ");
        stringBuilder.append(getPostnummerString(xmlGateAdresse));
        stringBuilder.append(' ');
        stringBuilder.append(kodeverk.getPoststed(getPostnummerString(xmlGateAdresse)));
        adresse.setAdresse(stringBuilder.toString());

        return adresse;
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

    private Adresse.Gateadresse tilGatedresse(XMLGateadresse xmlAdresse) {
        Adresse.Gateadresse adresse = new Adresse.Gateadresse();
        adresse.bolignummer = xmlAdresse.getBolignummer();
        adresse.kommunenummer = xmlAdresse.getKommunenummer();
        adresse.poststed = xmlAdresse.getPoststed() != null ? xmlAdresse.getPoststed().getValue() : "";
        adresse.gatenavn = xmlAdresse.getGatenavn();
        adresse.husnummer = xmlAdresse.getHusnummer() != null ? xmlAdresse.getHusnummer().toString() : "";

        return adresse;
    }

    private Adresse.MatrikkelAdresse tilMatrikkeladresse(XMLMatrikkeladresse xmlAdresse) {
        Adresse.MatrikkelAdresse adresse = new Adresse.MatrikkelAdresse();
        adresse.kommunenummer = xmlAdresse.getKommunenummer();
        adresse.bolignummer = xmlAdresse.getBolignummer();
        adresse.poststed = xmlAdresse.getPoststed() != null ? xmlAdresse.getPoststed().getValue() : "";
        adresse.eiendomsnavn = xmlAdresse.getEiendomsnavn();
        XMLMatrikkelnummer matrikkel = xmlAdresse.getMatrikkelnummer();
        if (matrikkel != null) {
            adresse.gaardsnummer = matrikkel.getGaardsnummer();
            adresse.bruksnummer = matrikkel.getBruksnummer();
            adresse.festenummer = matrikkel.getFestenummer();
            adresse.seksjonsnummer = matrikkel.getSeksjonsnummer();
            adresse.undernummer = matrikkel.getUndernummer();
        }
        return adresse;
    }

}
