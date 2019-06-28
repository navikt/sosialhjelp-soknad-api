package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;
import java.math.BigInteger;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class BrukerprofilServiceTest {
    private static final String IDENT = "56128349974";
    private static final String FOLKEREGISTRERT_ADRESSE_VALUE = "BOSTEDSADRESSE";
    private static final String EN_ADRESSE_GATE = "Grepalida";
    private static final String EN_ADRESSE_HUSNUMMER = "44";
    private static final String EN_ADRESSE_HUSBOKSTAV = "B";
    private static final String EN_ADRESSE_POSTNUMMER = "0560";
    private static final String EN_ADRESSE_POSTSTED = "Oslo";

    private static final Long EN_ANNEN_ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    private static final Long EN_ANNEN_ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();
    private static final String EN_ANNEN_ADRESSE_GATE = "Vegvegen";
    private static final String EN_ANNEN_ADRESSE_HUSNUMMER = "44";
    private static final String EN_ANNEN_ADRESSE_HUSBOKSTAV = "D";
    private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";

    private static final String EN_POSTBOKS_ADRESSEEIER = "Per Conradi";
    private static final String ET_POSTBOKS_NAVN = "Postboksstativet";
    private static final String EN_POSTBOKS_NUMMER = "66";

    private static final String EN_ADRESSELINJE = "Poitigatan 55";
    private static final String EN_ANNEN_ADRESSELINJE = "Nord-Poiti";
    private static final String EN_TREDJE_ADRESSELINJE = "1111";
    private static final String EN_FJERDE_ADRESSELINJE = "Helsinki";
    private static final String ET_LAND = "Finland";
    private static final String EN_LANDKODE = "FIN";
    private static final String ET_EIENDOMSNAVN = "Villastrøket";

    private static final String NORGE = "Norge";
    private static final String NORGE_KODE = "NOR";

    private static final String BANKKONTONUMMER = "123456789123";
    private static final String BANKNAVN = "Utlandsbanken A/S";

    @InjectMocks
    private BrukerprofilService brukerprofilService;

    @Mock
    private BrukerprofilPortType brukerprofilPortTypeMock;

    @Mock
    private Kodeverk kodeverkMock;

    private DateTimeFormatter dateTimeFormat;

    @Before
    public void setup() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getPoststed(EN_ANNEN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getLand(NORGE_KODE)).thenReturn(NORGE);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);

        dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
    }

    @Test
    public void mapResponsTilAdresserOgKontonummerMapperRiktigForNorskBankkonto() {
        final String forventetGjeldendeAdresse = EN_ADRESSE_GATE + " " + EN_ADRESSE_HUSNUMMER + EN_ADRESSE_HUSBOKSTAV + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        final String forventetFolkeregistrertAdresse = EN_ADRESSE_GATE + " " + EN_ADRESSE_HUSNUMMER + EN_ADRESSE_HUSBOKSTAV + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        final String forventetSekundarAdresse = "C/O " + EN_POSTBOKS_ADRESSEEIER + ", " + EN_ANNEN_ADRESSE_GATE + " " + EN_ANNEN_ADRESSE_HUSNUMMER + EN_ANNEN_ADRESSE_HUSBOKSTAV + ", "
                + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        AdresserOgKontonummer adresserOgKontonummer = brukerprofilService.mapResponsTilAdresserOgKontonummer(lagXMLHentKontaktinformasjonOgPreferanserResponse(false));

        assertThat(adresserOgKontonummer.getGjeldendeAdresse().getAdresse(), is(forventetGjeldendeAdresse));
        assertThat(adresserOgKontonummer.getSekundarAdresse().getAdresse(), is(forventetSekundarAdresse));
        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse().getAdresse(), is(forventetFolkeregistrertAdresse));
        assertThat(adresserOgKontonummer.getKontonummer(), is(BANKKONTONUMMER));
        assertThat(adresserOgKontonummer.getUtenlandskKontoBanknavn(), isEmptyString());
        assertThat(adresserOgKontonummer.getUtenlandskKontoLand(), isEmptyString());
        assertThat(adresserOgKontonummer.isUtenlandskBankkonto(), is(false));
    }

    @Test
    public void mapResponsTilAdresserOgKontonummerMapperUtenlandskBankkontoRiktig() {
        AdresserOgKontonummer adresserOgKontonummer = brukerprofilService.mapResponsTilAdresserOgKontonummer(lagXMLHentKontaktinformasjonOgPreferanserResponse(true));

        assertThat(adresserOgKontonummer.getKontonummer(), is(BANKKONTONUMMER));
        assertThat(adresserOgKontonummer.getUtenlandskKontoBanknavn(), is(BANKNAVN));
        assertThat(adresserOgKontonummer.getUtenlandskKontoLand(), is(ET_LAND));
        assertThat(adresserOgKontonummer.isUtenlandskBankkonto(), is(true));
    }

    @Test
    public void hentKontaktinfoOgPreferanserReturnererTomAdresserOgKontonummerHvisBrukerHarDiskresjonskode() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(brukerprofilPortTypeMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenThrow(new HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning());

        AdresserOgKontonummer adresserOgKontonummer = brukerprofilService.hentKontaktinformasjonOgPreferanser(IDENT);

        assertThat(adresserOgKontonummer.getFolkeregistrertAdresse().getAdresse(), nullValue());
        assertThat(adresserOgKontonummer.getSekundarAdresse().getAdresse(), nullValue());
        assertThat(adresserOgKontonummer.getGjeldendeAdresse().getAdresse(), nullValue());
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteMidlertidigOmrodeAdresseNorge() {
        final String forventetGjeldendeAdresse = ET_EIENDOMSNAVN + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedMidlertidigOmrodeAdresseSomGjeldendeAdresse());

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
        assertThat(gjeldendeAdresse.getAdressetype(), is(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE.name()));
        assertThat(gjeldendeAdresse.getGyldigFra(), is(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_FRA)));
        assertThat(gjeldendeAdresse.getGyldigTil(), is(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_TIL)));
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteMidlertidigPostboksAdresseNorge() {
        final String forventetGjeldendeAdresse = "C/O " + EN_POSTBOKS_ADRESSEEIER + ", Postboks "
                + EN_POSTBOKS_NUMMER + " " + ET_POSTBOKS_NAVN + ", " + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedMidlertidigPostboksAdresseSomGjeldendeAdresse());

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteFolkeregistretUtenlandskAdresse() {
        final String forventetGjeldendeAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", "
                + EN_FJERDE_ADRESSELINJE + ", " + ET_LAND;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedUtenlandskFolkeregistrertAdresseSomGjeldendeAdresse());

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteMidlertidigUtenlandskMidlertidigAdresseMed1Linjer() {
        final String forventetGjeldendeAdresse = EN_ADRESSELINJE + ", " + ET_LAND;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedMidlertidigUtenlandskAdresse(1));

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteMidlertidigUtenlandskMidlertidigAdresseMed2Linjer() {
        final String forventetGjeldendeAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + ET_LAND;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedMidlertidigUtenlandskAdresse(2));

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
    }

    @Test
    public void finnGjeldendeAdresseSkalStotteMidlertidigUtenlandskMidlertidigAdresseMed3Linjer() {
        final String forventetGjeldendeAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", " + ET_LAND;

        Adresse gjeldendeAdresse = brukerprofilService.finnGjeldendeAdresse(mockBrukerMedMidlertidigUtenlandskAdresse(3));

        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));
    }

    @Test(expected = TjenesteUtilgjengeligException.class)
    public void kasterApplicationExceptionVedWebserviceExceptionFraTps() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(brukerprofilPortTypeMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenThrow(new WebServiceException());

        brukerprofilService.hentKontaktinformasjonOgPreferanser(IDENT);
    }

    @Test(expected = IkkeFunnetException.class)
    public void kasterApplicationExceptionHvisPersonIkkeFinnesITPS() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(brukerprofilPortTypeMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenThrow(new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet());

        brukerprofilService.hentKontaktinformasjonOgPreferanser(IDENT);
    }

    private XMLHentKontaktinformasjonOgPreferanserResponse lagXMLHentKontaktinformasjonOgPreferanserResponse(boolean utenlandskBankkonto) {
        XMLBruker xmlBruker = mockBrukerMedFlereAdresser();
        if (utenlandskBankkonto) {
            xmlBruker.setBankkonto(lagUtenlandskBankkonto());
        } else {
            xmlBruker.setBankkonto(lagNorskBankkonto());
        }

        return new XMLHentKontaktinformasjonOgPreferanserResponse().withPerson(xmlBruker);
    }

    private XMLBruker mockBrukerMedFlereAdresser() {
        XMLBruker xmlBruker = new XMLBruker();
        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);

        xmlBruker.setBostedsadresse(bostedsadresse);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
        return xmlBruker;
    }

    private XMLBruker mockBrukerMedMidlertidigUtenlandskAdresse(int adresselinjer) {
        XMLBruker xmlBruker = new XMLBruker();
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
        return xmlBruker;
    }

    private XMLBruker mockBrukerMedUtenlandskFolkeregistrertAdresseSomGjeldendeAdresse() {
        XMLBruker xmlBruker = new XMLBruker();
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
        return xmlBruker;
    }

    private XMLBruker mockBrukerMedMidlertidigOmrodeAdresseSomGjeldendeAdresse() {
        XMLBruker xmlBruker = new XMLBruker();
        XMLMidlertidigPostadresseNorge midlertidigOmrodeAdresseNorge = generateMidlertidigOmrodeAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigOmrodeAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        return xmlBruker;
    }

    private XMLBruker mockBrukerMedMidlertidigPostboksAdresseSomGjeldendeAdresse() {
        XMLBruker xmlBruker = new XMLBruker();
        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        return xmlBruker;
    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigOmrodeAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigPostadresse = new XMLMidlertidigPostadresseNorge();

        XMLMatrikkeladresse xmlMatrikkelAdresse = new XMLMatrikkeladresse();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigPostadresse.setPostleveringsPeriode(xmlGyldighetsperiode);

        xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
        xmlMatrikkelAdresse.setPoststed(xmlpostnummer);
        xmlMatrikkelAdresse.setEiendomsnavn(ET_EIENDOMSNAVN);

        xmlMidlertidigPostadresse.setStrukturertAdresse(xmlMatrikkelAdresse);
        return xmlMidlertidigPostadresse;
    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigAdresseNorge() {
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
            xmlGyldighetsperiode.setFom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
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
        gateadresse.setLandkode(lagLandkode());
        bostedsadresse.setStrukturertAdresse(gateadresse);
        return bostedsadresse;
    }

    private XMLUstrukturertAdresse generateUstrukturertAdresseMedXAntallAdersseLinjer(int antallAdresseLinjer) {
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

    private static XMLLandkoder lagLandkode() {
        return new XMLLandkoder().withValue(NORGE);
    }

    private XMLBankkonto lagNorskBankkonto() {
        return new XMLBankkontoNorge()
                .withBankkonto(new XMLBankkontonummer()
                        .withBankkontonummer(BANKKONTONUMMER));
    }

    private XMLBankkonto lagUtenlandskBankkonto() {
        return new XMLBankkontoUtland()
                .withBankkontoUtland(new XMLBankkontonummerUtland()
                        .withBankkontonummer(BANKKONTONUMMER)
                        .withLandkode(new XMLLandkoder().withValue(EN_LANDKODE))
                        .withBanknavn(BANKNAVN));
    }
}