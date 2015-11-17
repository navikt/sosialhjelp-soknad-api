package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import org.joda.time.DateTime;
import org.mockito.Mock;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.util.List;

public class LagMockData {


    static final String RIKTIG_IDENT = "56128349974";
    static final String BARN_IDENT = "01010091736";
    static final String BARN_FORNAVN = "Bjarne";
    static final String BARN_ETTERNAVN = "Barnet";

    static final String FEIL_IDENT = "54321012345";
    static final String ET_FORNAVN = "Ola";
    static final String ET_MELLOMNAVN = "Johan";
    static final String ET_ETTERNAVN = "Normann";
    static final String FOLKEREGISTRERT_ADRESSE_VALUE = "BOSTEDSADRESSE";
    static final String EN_ADRESSE_GATE = "Grepalida";
    static final String EN_ADRESSE_HUSNUMMER = "44";
    static final String EN_ADRESSE_HUSBOKSTAV = "B";
    static final String EN_ADRESSE_POSTNUMMER = "0560";
    static final String EN_ADRESSE_POSTSTED = "Oslo";

    static final Long EN_ANNEN_ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    static final Long EN_ANNEN_ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();
    static final String EN_ANNEN_ADRESSE_GATE = "Vegvegen";
    static final String EN_ANNEN_ADRESSE_HUSNUMMER = "44";
    static final String EN_ANNEN_ADRESSE_HUSBOKSTAV = "D";
    static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";

    static final String EN_POSTBOKS_ADRESSEEIER = "Per Conradi";
    static final String ET_POSTBOKS_NAVN = "Postboksstativet";
    static final String EN_POSTBOKS_NUMMER = "66";

    static final String EN_ADRESSELINJE = "Poitigatan 55";
    static final String EN_ANNEN_ADRESSELINJE = "Nord-Poiti";
    static final String EN_TREDJE_ADRESSELINJE = "1111";
    static final String EN_FJERDE_ADRESSELINJE = "Helsinki";
    static final String ET_LAND = "Finland";
    static final String EN_LANDKODE = "FIN";
    static final String ET_EIEDOMSNAVN = "Villastrøket";

    static final String NORGE = "Norge";
    static final String NORGE_KODE = "NOR";

    @Mock
    static XMLBruker xmlBruker;

    static Person lagStandardPerson() {
        Person person = genererPersonMedGyldigIdentOgNavn(RIKTIG_IDENT, ET_FORNAVN, ET_ETTERNAVN);
        person.setFoedselsdato(fodseldato(1983, 12, 16));
        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN);

        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();

        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        familieRelasjoner.add(familierelasjon);

        return person;
    }

    static XMLBruker lagStandardXMLBrukerMedNorskIdent() {
        xmlBruker = new XMLBruker();
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
        xmlBruker.setIdent(xmlNorskIdent);

        return xmlBruker;
    }

    static void mockGyldigPersonMedMidlertidigUtenlandskAdresse(int adresselinjer) {
        XMLMidlertidigPostadresseUtland xmlMidlertidigPostadresseUtland = new XMLMidlertidigPostadresseUtland();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(adresselinjer);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_UTLAND");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);

        xmlMidlertidigPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);

        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigPostadresseUtland);

    }

    static void mockGyldigPersonMedUtenlandskFolkeregistrertAdresse() {
        XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(4);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("UTENLANDSK_ADRESSE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);

        xmlBruker.setPostadresse(xmlPostadresseUtland);

    }

    static void mockGyldigPersonMedMidlertidigOmrodeAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigOmrodeAdresseNorge = generateMidlertidigOmrodeAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigOmrodeAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    static void mockGyldigPersonMedMidlertidigPostboksAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    static void mockGyldigPersonMedAdresse() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);

        xmlBruker.setBostedsadresse(bostedsadresse);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    }

    static void mockGyldigPersonUtenNavn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        xmlBruker.setPersonnavn(personNavn);
    }

    static void mockGyldigPersonUtenMellomnavn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnUtenMellomnavn());
    }

    static void mockGyldigPerson() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }


    static XMLMidlertidigPostadresseNorge generateMidlertidigAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigNorge.setPostleveringsPeriode(xmlGyldighetsperiode);
        XMLGateadresse xmlgateadresse = new XMLGateadresse();
        xmlgateadresse.setTilleggsadresse(EN_POSTBOKS_ADRESSEEIER);
        xmlgateadresse.setGatenavn(EN_ANNEN_ADRESSE_GATE);
        xmlgateadresse.setHusnummer(new BigInteger(EN_ANNEN_ADRESSE_HUSNUMMER));
        xmlgateadresse.setHusbokstav(EN_ANNEN_ADRESSE_HUSBOKSTAV);
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
        xmlgateadresse.setPoststed(xmlpostnummer);
        xmlMidlertidigNorge.setStrukturertAdresse(xmlgateadresse);
        return xmlMidlertidigNorge;
    }

    static XMLMidlertidigPostadresseNorge generateMidlertidigOmrodeAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigPostadresse = new XMLMidlertidigPostadresseNorge();

        XMLMatrikkeladresse xmlMatrikkelAdresse = new XMLMatrikkeladresse();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigPostadresse.setPostleveringsPeriode(xmlGyldighetsperiode);

        xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
        xmlMatrikkelAdresse.setPoststed(xmlpostnummer);
        xmlMatrikkelAdresse.setEiendomsnavn(ET_EIEDOMSNAVN);

        xmlMidlertidigPostadresse.setStrukturertAdresse(xmlMatrikkelAdresse);
        return xmlMidlertidigPostadresse;
    }

    static XMLMidlertidigPostadresseNorge generateMidlertidigPostboksAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigNorge.setPostleveringsPeriode(xmlGyldighetsperiode);

        XMLPostboksadresseNorsk xmlPostboksAdresse = new XMLPostboksadresseNorsk();
        xmlPostboksAdresse.setPostboksanlegg(ET_POSTBOKS_NAVN);
        xmlPostboksAdresse.setPostboksnummer(EN_POSTBOKS_NUMMER);
        xmlPostboksAdresse.setTilleggsadresse(EN_POSTBOKS_ADRESSEEIER);
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
        xmlPostboksAdresse.setPoststed(xmlpostnummer);
        xmlMidlertidigNorge.setStrukturertAdresse(xmlPostboksAdresse);
        return xmlMidlertidigNorge;
    }

    static XMLGyldighetsperiode generateGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    static XMLBostedsadresse genererXMLFolkeregistrertAdresse(boolean medData) {
        XMLBostedsadresse bostedsadresse = new XMLBostedsadresse();
        XMLGateadresse gateadresse = new XMLGateadresse();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        if (medData) {
            gateadresse.setGatenavn(EN_ADRESSE_GATE);
            gateadresse.setHusnummer(new BigInteger(EN_ADRESSE_HUSNUMMER));
            gateadresse.setHusbokstav(EN_ADRESSE_HUSBOKSTAV);
            xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
        }
        gateadresse.setPoststed(xmlpostnummer);
        gateadresse.setLandkode(lagLandkode());
        bostedsadresse.setStrukturertAdresse(gateadresse);
        return bostedsadresse;
    }

    static XMLUstrukturertAdresse generateUstrukturertAdresseMedXAntallAdersseLinjer(int antallAdresseLinjer) {
        XMLUstrukturertAdresse ustrukturertAdresse = new XMLUstrukturertAdresse();
        switch (antallAdresseLinjer) {
            case 0:
                break;
            case 1:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                break;
            case 2:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                break;
            case 3:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje3(EN_TREDJE_ADRESSELINJE);
                break;
            case 4:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje3(EN_TREDJE_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje4(EN_FJERDE_ADRESSELINJE);
                break;
            default:
                break;
        }

        return ustrukturertAdresse;
    }

    static XMLLandkoder lagLandkode() {
        return new XMLLandkoder().withValue(NORGE);
    }

    static XMLPersonnavn navnMedMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setMellomnavn(ET_MELLOMNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
    }

    static XMLPersonnavn navnUtenMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
    }

    static Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String etternavn) {
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

    static Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        try {
            foedselsdato.setFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(year, month, day, 0));
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Klarte ikke å sette fødselsdato", e);
        }
        return foedselsdato;
    }

    static WSKontaktinformasjon genererDigitalKontaktinformasjonMedEpost() {
        return new WSKontaktinformasjon()
                .withPersonident(RIKTIG_IDENT)
                .withEpostadresse(new WSEpostadresse().withValue("test@test.com"))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue("12345678"))
                .withReservasjon("");
    }

    static WSKontaktinformasjon genererDigitalKontaktinformasjonUtenEpost() {
        return new WSKontaktinformasjon()
                .withPersonident(RIKTIG_IDENT)
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue("12345678"))
                .withReservasjon("");
    }
}

