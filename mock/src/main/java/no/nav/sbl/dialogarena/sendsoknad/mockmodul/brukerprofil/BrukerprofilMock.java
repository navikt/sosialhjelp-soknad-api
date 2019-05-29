package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrukerprofilMock {
    private static final String FODSELSNUMMER = "03076321565";
    private static final String FORNAVN = "Donald";
    private static final String MELLOMNAVN = "D.";
    private static final String ETTERNAVN = "Mockmann";
    private static final String EPOST = "test@epost.com";
    private static final String GATENAVN = "SANNERGATA"; // "Grepalida";
    private static final String HUSNUMMER = "2"; // "44";
    private static final String BOLIGNUMMER = "1234";
    private static final String POSTNUMMER = "0557"; // "0560";
    private static final String KOMMUNENUMMER = "0701";
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

    private static final String BANKKONTO_LANDKODE = "NOR";
    private static final String BANKKONTO_BANK = "Nordea";
    private static final String BANKKONTO_KONTONUMMER = "98769898765";

    public static final String POSTTYPE_NORSK = "BOSTEDSADRESSE";
    public static final String POSTTYPE_UTENLANDSK = "UTENLANDSK_ADRESSE";

    public enum Adressetyper {INGEN, NORSK, UTENLANDSK;}

    private static BrukerprofilMock brukerprofilMock = new BrukerprofilMock();
    private BrukerprofilPortTypeMock brukerprofilPortTypeMock = new BrukerprofilPortTypeMock();

    private static Map<String, XMLHentKontaktinformasjonOgPreferanserResponse> responses = new HashMap<>();


    public BrukerprofilPortType brukerProfilMock() {
        BrukerprofilPortType mock = mock(BrukerprofilPortType.class);

        try{
            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                    .thenAnswer((invocationOnMock) -> getOrCreateCurrentUserResponse());
        } catch(HentKontaktinformasjonOgPreferanserPersonIkkeFunnet | HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

    private static XMLHentKontaktinformasjonOgPreferanserResponse getOrCreateCurrentUserResponse() {
        XMLHentKontaktinformasjonOgPreferanserResponse respons = responses.get(OidcFeatureToggleUtils.getUserId());
        if (respons == null) {
            respons = createNewResponse();
            responses.put(OidcFeatureToggleUtils.getUserId(), respons);
        }


        return respons;
    }

    private static XMLHentKontaktinformasjonOgPreferanserResponse createNewResponse() {
        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
        xmlBruker.setGjeldendePostadresseType(new XMLPostadressetyper().withValue(""));

        settPostadresse(xmlBruker, Adressetyper.NORSK);
        settSekundarAdresse(xmlBruker, Adressetyper.UTENLANDSK);

        xmlBruker.setGjeldendePostadresseType(new XMLPostadressetyper().withValue("midlertidig"));
        settMidlertidigPostadresse(xmlBruker);

        return new XMLHentKontaktinformasjonOgPreferanserResponse().withPerson(xmlBruker);
    }

    public BrukerprofilMock(){

    }

    public static BrukerprofilMock getInstance(){
        return brukerprofilMock;
    }

    public BrukerprofilPortTypeMock getBrukerprofilPortTypeMock() {
        return brukerprofilPortTypeMock;
    }

    private static XMLGyldighetsperiode lagGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    private static void settMidlertidigPostadresse(XMLBruker xmlBruker) {
        final XMLMidlertidigPostadresse midlertidigPostadresse = new XMLMidlertidigPostadresseNorge()
                .withStrukturertAdresse(lagStrukturertPostadresse("42"));
        xmlBruker.setMidlertidigPostadresse(midlertidigPostadresse);
    }

    public void settMatrikkeladresse(XMLBruker xmlBruker) {
        final XMLBostedsadresse adresse = new XMLBostedsadresse().withStrukturertAdresse(new XMLMatrikkeladresse()
                .withEiendomsnavn("Eiendommens navn")
                .withKommunenummer("0301")
                .withLandkode(lagLandkode(LANDKODE))
                .withMatrikkelnummer(new XMLMatrikkelnummer()
                        .withGaardsnummer("1")
                        .withBruksnummer("2")
                        .withFestenummer("3")
                        .withSeksjonsnummer("4")
                        .withUndernummer("5")
                        )
                );
        xmlBruker.setBostedsadresse(adresse);
    }

    public static void settPostadresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        XMLPostadressetyper postAdresseType = xmlBruker.getGjeldendePostadresseType();
        if (adressetype.equals(Adressetyper.NORSK)) {
            XMLBostedsadresse adresse = new XMLBostedsadresse().withStrukturertAdresse(lagStrukturertPostadresse());
            xmlBruker.setBostedsadresse(adresse);
            postAdresseType.setValue(POSTTYPE_NORSK);
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setPostadresse(lagUtenlandskPostadresse(4));
            postAdresseType.setValue(POSTTYPE_UTENLANDSK);
        }
    }

    public static void settSekundarAdresse(XMLBruker xmlBruker, Adressetyper adressetype) {
        if (adressetype.equals(Adressetyper.NORSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigNorskPostadresse());
        } else if (adressetype.equals(Adressetyper.UTENLANDSK)) {
            xmlBruker.setMidlertidigPostadresse(lagMidlertidigUtenlandskPostadresse());
        } else {
            xmlBruker.setMidlertidigPostadresse(null);
        }
    }

    private static XMLMidlertidigPostadresseNorge lagMidlertidigNorskPostadresse() {
        XMLPostboksadresseNorsk postboks = new XMLPostboksadresseNorsk();
        postboks.setPostboksanlegg(POSTBOKS_NAVN);
        postboks.setPostboksnummer(POSTBOKS_NUMMER);
        postboks.setTilleggsadresse(POSTBOKS_ADRESSEEIER);
        postboks.setPoststed(lagPostnummer(POSTNUMMER));

        return new XMLMidlertidigPostadresseNorge()
                .withStrukturertAdresse(postboks)
                .withPostleveringsPeriode(lagGyldighetsperiode(false));
    }

    private static XMLMidlertidigPostadresseUtland lagMidlertidigUtenlandskPostadresse() {
        XMLMidlertidigPostadresseUtland postadresse = new XMLMidlertidigPostadresseUtland();
        postadresse.setUstrukturertAdresse(lagUstrukturertUtenlandskPostadresse(4));
        postadresse.setPostleveringsPeriode(lagGyldighetsperiode(false));
        return postadresse;
    }

    private static XMLPostadresse lagUtenlandskPostadresse(int antallLinjer) {
        XMLUstrukturertAdresse adresse = lagUstrukturertUtenlandskPostadresse(antallLinjer);
        return new XMLPostadresse().withUstrukturertAdresse(adresse);
    }

    private static XMLStrukturertAdresse lagStrukturertPostadresse() {
        return lagStrukturertPostadresse(HUSNUMMER);
    }

    private static XMLStrukturertAdresse lagStrukturertPostadresse(String husnummer) {
        return new XMLGateadresse()
                .withKommunenummer(KOMMUNENUMMER)
                .withBolignummer(BOLIGNUMMER)
                .withPoststed(new XMLPostnummer().withValue(POSTNUMMER))
                .withGatenavn(GATENAVN)
                .withHusnummer(new BigInteger(husnummer))
                .withLandkode(lagLandkode(LANDKODE));
    }

    private static XMLUstrukturertAdresse lagUstrukturertUtenlandskPostadresse(int antallLinjer) {
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

    private static XMLPostnummer lagPostnummer(String postnummer) {
        XMLPostnummer xmlPostnummer = new XMLPostnummer();
        xmlPostnummer.setValue(postnummer);
        return xmlPostnummer;
    }

    private static XMLLandkoder lagLandkode(String landkode) {
        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(landkode);
        return xmlLandkode;
    }


    private static XMLBruker genererXmlBrukerMedGyldigIdentOgNavn(boolean medMellomnavn) {
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

        return xmlBruker;
    }

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EPOST);
    }


    private static XMLBankkonto bankkonto(String kontonummer) {
        XMLLandkoder landkoder = new XMLLandkoder();
        landkoder.setValue(BANKKONTO_LANDKODE);

        XMLBankkontonummer bankkontonummer = new XMLBankkontonummer();
        bankkontonummer.setBanknavn(BANKKONTO_BANK);
        bankkontonummer.setBankkontonummer(kontonummer);

        XMLBankkontoNorge bankkonto = new XMLBankkontoNorge();
        bankkonto.setBankkonto(bankkontonummer);

        return bankkonto;
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

    public static void setBrukerprofil(String jsonBrukerprofil) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(XMLStrukturertAdresse.class, new XMLStrukturertAdresseDeserializer());
            module.addDeserializer(XMLStedsadresse.class, new XMLStedsadresseDeserializer());
            module.addDeserializer(XMLPostboksadresse.class, new XMLPostboksadresseDeserializer());
            module.addDeserializer(XMLStedsadresseNorge.class, new XMLStedsadresseNorgeDeserializer());
            module.addDeserializer(XMLPerson.class, new XMLPersonDeserializer());
            module.addDeserializer(XMLElektroniskAdresse.class, new XMLElektroniskAdresseDeserializer());
            module.addDeserializer(XMLMidlertidigPostadresse.class, new XMLMidlertidigPostadresseDeserializer());
            module.addDeserializer(XMLBankkonto.class, new XMLBankkontoDeserializer());
            mapper.registerModule(module);

            XMLHentKontaktinformasjonOgPreferanserResponse newResponse = mapper.readValue(jsonBrukerprofil, XMLHentKontaktinformasjonOgPreferanserResponse.class);
            XMLHentKontaktinformasjonOgPreferanserResponse currentResponse = getOrCreateCurrentUserResponse();

            if (currentResponse == null){
                responses.put(OidcFeatureToggleUtils.getUserId(), newResponse);
            } else {
                responses.replace(OidcFeatureToggleUtils.getUserId(), newResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void resetBrukerprofil(){
        XMLHentKontaktinformasjonOgPreferanserResponse response = getOrCreateCurrentUserResponse();
        if (response == null){
            responses.put(OidcFeatureToggleUtils.getUserId(), response);
        } else {
            responses.replace(OidcFeatureToggleUtils.getUserId(), response);
        }
    }
}
