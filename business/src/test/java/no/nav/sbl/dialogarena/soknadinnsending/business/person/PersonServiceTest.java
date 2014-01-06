package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMatrikkeladresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonServiceTest {

    public static final String ADRESSER = "adresser";
    @InjectMocks
    private PersonServiceTPS service;

    @Mock
    private BrukerprofilPortType brukerprofilMock;
    @Mock
    private Kodeverk kodeverkMock;

    private static final String RIKTIG_IDENT = "12345";
    private static final String FEIL_IDENT = "54321";
    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
    private static final String FOLKEREGISTRERT_ADRESSE_VALUE = "BOSTEDSADRESSE";
    private static final String MIDLERTIDIG_POSTADRESSE_NORGE_VALUE = "MIDLERTIDIG_POSTADRESSE_NORGE";
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
    private static final List<String> EN_ADRESSE_UTLANDET = Arrays.asList(EN_ADRESSELINJE);
    private static final List<String> EN_ANNEN_ADRESSE_UTLANDET = Arrays.asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE);
    private static final List<String> EN_TREDJE_ADRESSE_UTLANDET = Arrays.asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE, EN_TREDJE_ADRESSELINJE);
    private static final List<String> EN_FJERDE_ADRESSE_UTLANDET = Arrays.asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE, EN_TREDJE_ADRESSELINJE, EN_FJERDE_ADRESSELINJE);
    private static final String ET_LAND = "Finland";
    private static final String EN_LANDKODE = "FIN";
    private static final String ET_EIEDOMSNAVN = "Villastrøket";
    private static final String EN_EPOST = "test@epost.com";

    @SuppressWarnings("unchecked")
    @Test
    public void returnerPersonUtenDataHvisPersonenSomReturneresHarFeilIdent() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = new XMLHentKontaktinformasjonOgPreferanserRequest();
        request.setIdent(FEIL_IDENT);
        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenThrow(HentKontaktinformasjonOgPreferanserPersonIkkeFunnet.class);
        Person person = service.hentPerson(1l, FEIL_IDENT);
        Assert.assertNotNull(person);
    }

    @Test
    public void returnererPersonObjektDersomPersonenSomReturneresHarRiktigIdent() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l, RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum fnr = (Faktum) hentetPerson.getFakta().get("fnr");
        Faktum fornavn = (Faktum) hentetPerson.getFakta().get("fornavn");
        Faktum mellomnavn = (Faktum) hentetPerson.getFakta().get("mellomnavn");
        Faktum etternavnavn = (Faktum) hentetPerson.getFakta().get("etternavn");
        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
        Faktum epost = (Faktum) hentetPerson.getFakta().get("epost");
        Assert.assertEquals(RIKTIG_IDENT, fnr.getValue());
        Assert.assertEquals(ET_FORNAVN, fornavn.getValue());
        Assert.assertEquals(ET_MELLOMNAVN, mellomnavn.getValue());
        Assert.assertEquals(ET_ETTERNAVN, etternavnavn.getValue());
        Assert.assertEquals(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN, sammensattnavn.getValue());
        Assert.assertEquals(EN_EPOST, epost.getValue());
    }

    @Test
    public void skalStottePersonerUtenMellomnavn() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(false);

        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l, RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum mellomnavn = (Faktum) hentetPerson.getFakta().get("mellomnavn");
        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
        Assert.assertEquals("", mellomnavn.getValue());
        Assert.assertEquals(ET_FORNAVN + " " + ET_ETTERNAVN, sammensattnavn.getValue());
    }

    @Test
    public void skalStottePersonerUtenNavn() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();


        XMLBruker xmlBruker = new XMLBruker();
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l, RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
        Assert.assertEquals("", sammensattnavn.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonObjektMedAdresseInformasjon() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        xmlBruker.setBostedsadresse(bostedsadresse);

        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        Person hentetPerson = service.hentPerson(3l, RIKTIG_IDENT);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_ADRESSE_GATE, adresseliste.get(0).getGatenavn());
        Assert.assertEquals(EN_ADRESSE_HUSNUMMER, adresseliste.get(0).getHusnummer());
        Assert.assertEquals(EN_ADRESSE_HUSBOKSTAV, adresseliste.get(0).getHusbokstav());
        Assert.assertEquals(EN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
        Assert.assertEquals(EN_ADRESSE_POSTSTED, adresseliste.get(0).getPoststed());
        Assert.assertEquals(FOLKEREGISTRERT_ADRESSE_VALUE, adresseliste.get(0).getType().toString());
        Assert.assertTrue(adresseliste.size() > 1);
        Assert.assertEquals(MIDLERTIDIG_POSTADRESSE_NORGE_VALUE, adresseliste.get(1).getType().toString());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(1).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(1).getGyldigTil());
        Assert.assertEquals(EN_POSTBOKS_ADRESSEEIER, adresseliste.get(1).getAdresseEier());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GATE, adresseliste.get(1).getGatenavn());
        Assert.assertEquals(EN_ANNEN_ADRESSE_HUSNUMMER, adresseliste.get(1).getHusnummer());
        Assert.assertEquals(EN_ANNEN_ADRESSE_HUSBOKSTAV, adresseliste.get(1).getHusbokstav());
        Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(1).getPostnummer());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigOmrodeAdresseNorge() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLMidlertidigPostadresseNorge midlertidigOmrodeAdresseNorge = generateMidlertidigOmrodeAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigOmrodeAdresseNorge);

        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(4l, RIKTIG_IDENT);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);

        Assert.assertEquals(EN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
        Assert.assertEquals(ET_EIEDOMSNAVN, adresseliste.get(0).getEiendomsnavn());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigPostboksAdresseNorge() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge(true);
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);

        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(4l, RIKTIG_IDENT);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_POSTBOKS_ADRESSEEIER, adresseliste.get(0).getAdresseEier());
        Assert.assertEquals(ET_POSTBOKS_NAVN, adresseliste.get(0).getPostboksNavn());
        Assert.assertEquals(EN_POSTBOKS_NUMMER, adresseliste.get(0).getPostboksNummer());
        Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigPostboksAdresseNorgeMedMangeManglendeFelter() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge(false);
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);

        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(4l, RIKTIG_IDENT);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(null, adresseliste.get(0).getAdresseEier());
        Assert.assertEquals(null, adresseliste.get(0).getPostboksNavn());
        Assert.assertEquals(null, adresseliste.get(0).getPostboksNummer());
        Assert.assertEquals(null, adresseliste.get(0).getPostnummer());
        Assert.assertEquals(null, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteFolkeregistretUtenlandskAdresse() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
        XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(4);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        xmlPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);
        xmlBruker.setPostadresse(xmlPostadresseUtland);
        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);
        Person hentetPerson = service.hentPerson(5l, RIKTIG_IDENT);

        Assert.assertNotNull(hentetPerson.getFakta());
        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_FJERDE_ADRESSE_UTLANDET, adresseliste.get(0).getUtenlandsAdresse());
        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed0Linjer() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {

        Person hentetPerson = skalStotteMidlertidigUtenlandskMidlertidigAdresser(0);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(Arrays.asList(), adresseliste.get(0).getUtenlandsAdresse());
        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed1Linje() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {

        Person hentetPerson = skalStotteMidlertidigUtenlandskMidlertidigAdresser(1);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_ADRESSE_UTLANDET, adresseliste.get(0).getUtenlandsAdresse());
        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed2Linjer() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {

        Person hentetPerson = skalStotteMidlertidigUtenlandskMidlertidigAdresser(2);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_ANNEN_ADRESSE_UTLANDET, adresseliste.get(0).getUtenlandsAdresse());
        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed3Linjer() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {

        Person hentetPerson = skalStotteMidlertidigUtenlandskMidlertidigAdresser(3);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_TREDJE_ADRESSE_UTLANDET, adresseliste.get(0).getUtenlandsAdresse());
        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed4Linjer() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {

        Person hentetPerson = skalStotteMidlertidigUtenlandskMidlertidigAdresser(4);

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(EN_FJERDE_ADRESSE_UTLANDET, adresseliste.get(0).getUtenlandsAdresse());

        Assert.assertEquals(ET_LAND, adresseliste.get(0).getLand());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteFolkeregistrertOgMidlertidigAdresseMedMidlertidigSattSomGjeldende() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(false);
        xmlBruker.setBostedsadresse(bostedsadresse);

        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(MIDLERTIDIG_POSTADRESSE_NORGE_VALUE);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        Person hentetPerson = service.hentPerson(3l, RIKTIG_IDENT);

        Faktum gjeldendeAdresseType = (Faktum) hentetPerson.getFakta().get("gjeldendeAdresseType");
        Assert.assertEquals(MIDLERTIDIG_POSTADRESSE_NORGE_VALUE, gjeldendeAdresseType.getValue());

        List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get(ADRESSER);
        Assert.assertNotNull(adresseliste);
        Assert.assertEquals(FOLKEREGISTRERT_ADRESSE_VALUE, adresseliste.get(0).getType().toString());
        Assert.assertEquals(null, adresseliste.get(0).getGatenavn());
        Assert.assertEquals("", adresseliste.get(0).getHusnummer());
        Assert.assertEquals("", adresseliste.get(0).getHusbokstav());
        Assert.assertEquals(null, adresseliste.get(0).getPostnummer());
        Assert.assertEquals(null, adresseliste.get(0).getPoststed());
        Assert.assertTrue(adresseliste.size() > 1);
        Assert.assertEquals(MIDLERTIDIG_POSTADRESSE_NORGE_VALUE, adresseliste.get(1).getType().toString());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(1).getGyldigFra());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(1).getGyldigTil());
        Assert.assertEquals(EN_POSTBOKS_ADRESSEEIER, adresseliste.get(1).getAdresseEier());
        Assert.assertEquals(EN_ANNEN_ADRESSE_GATE, adresseliste.get(1).getGatenavn());
        Assert.assertEquals(EN_ANNEN_ADRESSE_HUSNUMMER, adresseliste.get(1).getHusnummer());
        Assert.assertEquals(EN_ANNEN_ADRESSE_HUSBOKSTAV, adresseliste.get(1).getHusbokstav());
        Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(1).getPostnummer());
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

    private XMLMidlertidigPostadresseNorge generateMidlertidigPostboksAdresseNorge(boolean medData) {
        XMLMidlertidigPostadresseNorge xmlMidlertidigPostboksNorge = new XMLMidlertidigPostadresseNorge();

        XMLPostboksadresseNorsk xmlpostboksadresse = new XMLPostboksadresseNorsk();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(medData);
        xmlMidlertidigPostboksNorge.setPostleveringsPeriode(xmlGyldighetsperiode);
        if (medData) {
            xmlpostboksadresse.setTilleggsadresse(EN_POSTBOKS_ADRESSEEIER);
            xmlpostboksadresse.setPostboksanlegg(ET_POSTBOKS_NAVN);
            xmlpostboksadresse.setPostboksnummer(EN_POSTBOKS_NUMMER);
            xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
        }
        xmlpostboksadresse.setPoststed(xmlpostnummer);
        xmlMidlertidigPostboksNorge.setStrukturertAdresse(xmlpostboksadresse);
        return xmlMidlertidigPostboksNorge;


    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigOmrodeAdresseNorge() {
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

    private XMLGyldighetsperiode generateGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
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

        return xmlBruker;
    }

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EN_EPOST);
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
        return bostedsadresse;
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest hentRequestMedGyldigIdent() {
        XMLHentKontaktinformasjonOgPreferanserRequest request = new XMLHentKontaktinformasjonOgPreferanserRequest();
        request.setIdent(RIKTIG_IDENT);
        return request;
    }

    private Person skalStotteMidlertidigUtenlandskMidlertidigAdresser(int antallAdresseLinjer) throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();

        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

        XMLMidlertidigPostadresseUtland midlertidigPostboksAdresseUtlandet = generateMidlertidigAdresseUtlandet(antallAdresseLinjer);
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseUtlandet);

        response.setPerson(xmlBruker);

        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);
        Person hentetPerson = service.hentPerson(5l, RIKTIG_IDENT);

        return hentetPerson;
    }

    private XMLMidlertidigPostadresseUtland generateMidlertidigAdresseUtlandet(int antallAdresseLinjer) {
        XMLMidlertidigPostadresseUtland xmlMidlertidigAdresseUtland = new XMLMidlertidigPostadresseUtland();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigAdresseUtland.setPostleveringsPeriode(xmlGyldighetsperiode);

        XMLUstrukturertAdresse ustrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(antallAdresseLinjer);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        ustrukturertAdresse.setLandkode(xmlLandkode);
        xmlMidlertidigAdresseUtland.setUstrukturertAdresse(ustrukturertAdresse);

        return xmlMidlertidigAdresseUtland;
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
}
