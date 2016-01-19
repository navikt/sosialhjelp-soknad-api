package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;

import java.math.BigInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrukerprofilMock {
    private static final String FODSELSNUMMER = "***REMOVED***";
    private static final String FORNAVN = "Donald";
    private static final String MELLOMNAVN = "D.";
    private static final String ETTERNAVN = "Mockmann";
    private static final String EPOST = "test@epost.com";
    private static final String GATENAVN = "Grepalida";
    private static final String HUSNUMMER = "44";
    private static final String HUSBOKSTAV = "B";
    private static final String POSTNUMMER = "0560";
    private static final String LANDKODE = "NOR";
    private static final String UTENLANDSK_LANDKODE = "FIN";
    private static final String ADRESSELINJE1 = "Poitigatan 55";
    private static final String ADRESSELINJE2 = "Nord-Poiti";
    private static final String ADRESSELINJE3 = "1111 Helsinki";
    private static final String ADRESSELINJE4 = "Finland";

    private static final String POSTBOKS_ADRESSEEIER = "Per Conradi";
    private static final String POSTBOKS_NAVN = "Postboksstativet";
    private static final String POSTBOKS_NUMMER = "66";
    private static final Long ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    private static final Long ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();

    public enum Adressetyper {INGEN, NORSK, UTENLANDSK;}

    private static BrukerprofilMock brukerprofilMock = new BrukerprofilMock();

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EPOST);
    }

    private BrukerprofilMock(){

    }

    public static BrukerprofilMock getInstance(){
        return brukerprofilMock;
    }

    public BrukerprofilPortType getBrukerprofilPortTypeMock() {
        BrukerprofilPortType mock = mock(BrukerprofilPortType.class);
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
        xmlBruker.setGjeldendePostadresseType(new XMLPostadressetyper());

        settPostadresse(xmlBruker, Adressetyper.NORSK);
        settSekundarAdresse(xmlBruker, Adressetyper.UTENLANDSK);

        response.setPerson(xmlBruker);

        try {
            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(response);
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet | HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    private XMLGyldighetsperiode lagGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    public void settBostedsadresse(XMLBruker xmlBruker) {
        xmlBruker.setBostedsadresse(lagBostedsAdresse());
        xmlBruker.getGjeldendePostadresseType().setValue("BOSTEDSADRESSE");
    }

    public void slettBostedsadresse(XMLBruker xmlBruker) {
        xmlBruker.setBostedsadresse(null);
    }

    public void settPostadresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        XMLPostadressetyper postAdresseType = xmlBruker.getGjeldendePostadresseType();
        if (adressetype.equals(Adressetyper.NORSK)) {
            xmlBruker.setPostadresse(lagPostadresse(3, false));
            postAdresseType.setValue("POSTADRESSE");
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setPostadresse(lagPostadresse(4, true));
            postAdresseType.setValue("UTENLANDSK_ADRESSE");
        } else {
            xmlBruker.setPostadresse(null);
        }
    }

    public void settSekundarAdresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        if (adressetype.equals(Adressetyper.NORSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigNorskPostadresse());
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigUtenlandskPostadresse());
        } else {
            xmlBruker.setPostadresse(null);
        }
    }

    private XMLBostedsadresse lagBostedsAdresse() {
        XMLBostedsadresse xmlBostedsadresse = new XMLBostedsadresse();
        xmlBostedsadresse.setStrukturertAdresse(lagStrukturertGateAdresse());
        return xmlBostedsadresse;
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
        postadresse.setUstrukturertAdresse(lagUstrukturertPostadresse(4, true));
        postadresse.setPostleveringsPeriode(lagGyldighetsperiode(false));
        return postadresse;
    }

    private XMLPostadresse lagPostadresse(int antallLinjer, boolean utenlandsk) {
        XMLPostadresse xmlPostadresse = new XMLPostadresse();
        xmlPostadresse.setUstrukturertAdresse(lagUstrukturertPostadresse(antallLinjer, utenlandsk));
        return xmlPostadresse;
    }

    private XMLUstrukturertAdresse lagUstrukturertPostadresse(int antallLinjer, boolean utenlandsk) {
        XMLUstrukturertAdresse xmlUstrukturertAdresse = new XMLUstrukturertAdresse();
        xmlUstrukturertAdresse.setLandkode(lagLandkode(utenlandsk ? UTENLANDSK_LANDKODE : LANDKODE));

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

    private XMLGateadresse lagStrukturertGateAdresse() {
        XMLGateadresse xmlGateadresse = new XMLGateadresse();
        xmlGateadresse.setGatenavn(GATENAVN);
        xmlGateadresse.setHusnummer(new BigInteger(HUSNUMMER));
        xmlGateadresse.setHusbokstav(HUSBOKSTAV);
        xmlGateadresse.setPoststed(lagPostnummer(POSTNUMMER));
        xmlGateadresse.setLandkode(lagLandkode(LANDKODE));
        return xmlGateadresse;
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
        landkoder.setValue("DNK");

        XMLBankkontonummerUtland bankkontonummer = new XMLBankkontonummerUtland();
        bankkontonummer.setBanknavn("Nordea");
        bankkontonummer.setBankkontonummer("9876 98 98765");
        bankkontonummer.setLandkode(landkoder);

        XMLBankkontoUtland bankkonto = new XMLBankkontoUtland();
        bankkonto.setBankkontoUtland(bankkontonummer);

        return bankkonto;
    }
}
