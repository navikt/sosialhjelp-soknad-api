package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import org.joda.time.DateTime;

public class BrukerprofilMock {
    private static final String FODSELSNUMMER = "03076321565";
    private static final String FORNAVN = "Donald";
    private static final String MELLOMNAVN = "D.";
    private static final String ETTERNAVN = "Mockmann";
    private static final String EPOST = "test@epost.com";
    private static final String GATENAVN = "Grepalida";
    private static final String HUSNUMMER = "44";
    private static final String HUSBOKSTAV = "B";
    private static final String POSTNUMMER = "0560";
    private static final String POSTSTED = "Oslo";
    private static final String LANDKODE = "NOR";
    private static final String LANDKODE_UTENLANDSK = "FIN";
    private static final String ADRESSELINJE1 = "Poitigatan 55";
    private static final String ADRESSELINJE2 = "Nord-Poiti";
    private static final String ADRESSELINJE3 = "1111 Helsinki";
    private static final String ADRESSELINJE4 = "Finland";

    private static final String POSTBOKS_ADRESSEEIER = "Per Conradi";
    private static final String POSTBOKS_NAVN = "Postboksstativet";
    private static final String POSTBOKS_NUMMER = "66";
    private static final Long ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    private static final Long ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();

    private static final String BANKKONTO_LANDKODE = "";
    private static final String BANKKONTO_BANK = "Nodea";
    private static final String BANKKONTO_KONTONUMMER = "9876 98 98765";

    public static final String POSTTYPE_NORSK = "POSTADRESSE";
    public static final String POSTTYPE_UTENLANDSK = "UTENLANDSK_ADRESSE";

    public enum Adressetyper {INGEN, NORSK, UTENLANDSK;}

    private static BrukerprofilMock brukerprofilMock = new BrukerprofilMock();
    private BrukerprofilPortTypeMock brukerprofilPortTypeMock = new BrukerprofilPortTypeMock();

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EPOST);
    }

    private BrukerprofilMock(){
        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
        xmlBruker.setGjeldendePostadresseType(new XMLPostadressetyper());

        settPostadresse(xmlBruker, Adressetyper.NORSK);
        settSekundarAdresse(xmlBruker, Adressetyper.UTENLANDSK);

        brukerprofilPortTypeMock.setPerson(xmlBruker);
    }

    public static BrukerprofilMock getInstance(){
        return brukerprofilMock;
    }

    public BrukerprofilPortTypeMock getBrukerprofilPortTypeMock() {
        return brukerprofilPortTypeMock;
    }

    private XMLGyldighetsperiode lagGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    public void settPostadresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        XMLPostadressetyper postAdresseType = xmlBruker.getGjeldendePostadresseType();
        if (adressetype.equals(Adressetyper.NORSK)) {
            XMLPostadresse postadresse = new XMLPostadresse().withUstrukturertAdresse(lagUstrukturertPostadresse());
            xmlBruker.setPostadresse(postadresse);
            postAdresseType.setValue(POSTTYPE_NORSK);
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setPostadresse(lagUtenlandskPostadresse(4));
            postAdresseType.setValue(POSTTYPE_UTENLANDSK);
        }
    }

    public void settSekundarAdresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        if (adressetype.equals(Adressetyper.NORSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigNorskPostadresse());
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigUtenlandskPostadresse());
        } else {
            xmlBruker.setMidlertidigPostadresse(null);
        }
    }

    private XMLMidlertidigPostadresseNorge lagMidlertidigNorskPostadresse() {
        XMLPostboksadresseNorsk postboks = new XMLPostboksadresseNorsk();
        postboks.setPostboksanlegg(POSTBOKS_NAVN);
        postboks.setPostboksnummer(POSTBOKS_NUMMER);
        postboks.setTilleggsadresse(POSTBOKS_ADRESSEEIER);
        postboks.setPoststed(lagPostnummer(POSTNUMMER));

        return new XMLMidlertidigPostadresseNorge()
                .withStrukturertAdresse(postboks)
                .withPostleveringsPeriode(lagGyldighetsperiode(false));
    }

    private XMLMidlertidigPostadresseUtland lagMidlertidigUtenlandskPostadresse() {
        XMLMidlertidigPostadresseUtland postadresse = new XMLMidlertidigPostadresseUtland();
        postadresse.setUstrukturertAdresse(lagUstrukturertUtenlandskPostadresse(4));
        postadresse.setPostleveringsPeriode(lagGyldighetsperiode(false));
        return postadresse;
    }

    private XMLPostadresse lagUtenlandskPostadresse(int antallLinjer) {
        XMLUstrukturertAdresse adresse = lagUstrukturertUtenlandskPostadresse(antallLinjer);
        return new XMLPostadresse().withUstrukturertAdresse(adresse);
    }

    private XMLUstrukturertAdresse lagUstrukturertPostadresse() {
        XMLUstrukturertAdresse xmlUstrukturertAdresse = new XMLUstrukturertAdresse();
        xmlUstrukturertAdresse.setAdresselinje1(String.format("%s %s%s", GATENAVN, HUSNUMMER, HUSBOKSTAV));
        xmlUstrukturertAdresse.setAdresselinje2(String.format("%s %s", POSTNUMMER, POSTSTED));
        xmlUstrukturertAdresse.setLandkode(lagLandkode(LANDKODE));
        return xmlUstrukturertAdresse;
    }

    private XMLUstrukturertAdresse lagUstrukturertUtenlandskPostadresse(int antallLinjer) {
        XMLUstrukturertAdresse xmlUstrukturertAdresse = new XMLUstrukturertAdresse();
        xmlUstrukturertAdresse.setLandkode(lagLandkode(LANDKODE_UTENLANDSK));

        if (antallLinjer >= 1) {
            xmlUstrukturertAdresse.setAdresselinje1(ADRESSELINJE1);
        }
        if (antallLinjer >= 2) {
            xmlUstrukturertAdresse.setAdresselinje2(ADRESSELINJE2);
        }
        if (antallLinjer >= 3) {
            xmlUstrukturertAdresse.setAdresselinje3(ADRESSELINJE3);
        }
        if (antallLinjer >= 4) {
            xmlUstrukturertAdresse.setAdresselinje4(ADRESSELINJE4);
        }
        return xmlUstrukturertAdresse;
    }

    private XMLPostnummer lagPostnummer(String postnummer) {
        XMLPostnummer xmlPostnummer = new XMLPostnummer();
        xmlPostnummer.setValue(postnummer);
        return xmlPostnummer;
    }

    private XMLLandkoder lagLandkode(String landkode) {
        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(landkode);
        return xmlLandkode;
    }

    private XMLBruker genererXmlBrukerMedGyldigIdentOgNavn(boolean medMellomnavn) {
        XMLBruker xmlBruker = new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(FORNAVN);
        if (medMellomnavn) {
            personNavn.setMellomnavn(MELLOMNAVN);
            personNavn.setSammensattNavn(FORNAVN + " " + MELLOMNAVN + " " + ETTERNAVN);
        } else {
            personNavn.setMellomnavn("");
            personNavn.setSammensattNavn(FORNAVN + " " + ETTERNAVN);
        }
        personNavn.setEtternavn(ETTERNAVN);
        xmlBruker.setPersonnavn(personNavn);
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(FODSELSNUMMER);
        xmlBruker.setIdent(xmlNorskIdent);

        xmlBruker.setBankkonto(utenlandskBankkonto());

        return xmlBruker;
    }

    private XMLBankkonto utenlandskBankkonto() {
        XMLLandkoder landkoder = new XMLLandkoder();
        landkoder.setValue(BANKKONTO_LANDKODE);

        XMLBankkontonummerUtland bankkontonummer = new XMLBankkontonummerUtland();
        bankkontonummer.setBanknavn(BANKKONTO_BANK);
        bankkontonummer.setBankkontonummer(BANKKONTO_KONTONUMMER);
        bankkontonummer.setLandkode(landkoder);

        XMLBankkontoUtland bankkonto = new XMLBankkontoUtland();
        bankkonto.setBankkontoUtland(bankkontonummer);

        return bankkonto;
    }
}
