package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkonto;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummerUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;

import java.math.BigInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BrukerprofilMock {

    private static final String RIKTIG_IDENT = "03076321565";
    private static final String ET_FORNAVN = "Donald";
    private static final String ET_MELLOMNAVN = "D.";
    private static final String ET_ETTERNAVN = "Mockmann";
    private static final String EN_EPOST = "test@epost.com";
    private static final String EN_ADRESSE_GATE = "Grepalida";
    private static final String EN_ADRESSE_HUSNUMMER = "44";
    private static final String EN_ADRESSE_HUSBOKSTAV = "B";
    private static final String EN_ADRESSE_POSTNUMMER = "0560";
    private static final String EN_ADRESSELINJE = "Poitigatan 55";
    private static final String EN_ANNEN_ADRESSELINJE = "Nord-Poiti";
    private static final String EN_TREDJE_ADRESSELINJE = "1111 Helsinki";
    private static final String EN_FJERDE_ADRESSELINJE = "Finland";

    private static final String EN_POSTBOKS_ADRESSEEIER = "Per Conradi";
    private static final String ET_POSTBOKS_NAVN = "Postboksstativet";
    private static final String EN_POSTBOKS_NUMMER = "66";
    private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";
    private static final Long EN_ANNEN_ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    private static final Long EN_ANNEN_ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();


    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EN_EPOST);
    }

    private static XMLLandkoder lagLandkode(String landkode) {
        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(landkode);
        return xmlLandkode;
    }

    public BrukerprofilPortType brukerprofilMock() {
        BrukerprofilPortType mock = mock(BrukerprofilPortType.class);
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        settAdresse(xmlBruker, "BOSTEDSADRESSE");
        settSekundarAdresse(xmlBruker);

        response.setPerson(xmlBruker);

        try {
            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(response);
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet ikkeFunnet) {
            throw new RuntimeException(ikkeFunnet);
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning sikkerhetsbegrensning) {
            throw new RuntimeException(sikkerhetsbegrensning);
        }
        return mock;
    }

    private void settSekundarAdresse(XMLBruker xmlBruker) {
        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");

        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigPostboksAdresseNorge() {
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

    private XMLGyldighetsperiode generateGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(
                    EN_ANNEN_ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    private void settAdresse(XMLBruker xmlBruker, String type) {
        if ("BOSTEDSADRESSE".equals(type)) {
            XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
            xmlBruker.setBostedsadresse(bostedsadresse);

            XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
            postadressetyper.setValue("BOSTEDSADRESSE");
            xmlBruker.setGjeldendePostadresseType(postadressetyper);
        } else if ("UTENLANDSK_ADRESSE".equals(type)) {
            XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
            XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(4);

            utenlandskUstrukturertAdresse.setLandkode(lagLandkode("FIN"));

            xmlPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);
            xmlBruker.setPostadresse(xmlPostadresseUtland);

            XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
            postadressetyper.setValue("POSTADRESSE");
            xmlBruker.setGjeldendePostadresseType(postadressetyper);
        }
    }

    private XMLUstrukturertAdresse generateUstrukturertAdresseMedXAntallAdersseLinjer(
            int antallAdresseLinjer) {
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

    private XMLBostedsadresse genererXMLFolkeregistrertAdresse(boolean medData) {
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
        bostedsadresse.setStrukturertAdresse(gateadresse);
        gateadresse.setLandkode(lagLandkode("NOR"));
        return bostedsadresse;
    }

    private XMLBruker genererXmlBrukerMedGyldigIdentOgNavn(boolean medMellomnavn) {
        XMLBruker xmlBruker = new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        if (medMellomnavn) {
            personNavn.setMellomnavn(ET_MELLOMNAVN);
            personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN);
        } else {
            personNavn.setMellomnavn("");
            personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_ETTERNAVN);
        }
        personNavn.setEtternavn(ET_ETTERNAVN);
        xmlBruker.setPersonnavn(personNavn);
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
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
