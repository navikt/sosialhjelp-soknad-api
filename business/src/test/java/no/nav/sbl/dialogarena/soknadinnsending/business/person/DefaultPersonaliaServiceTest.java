package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonConnector;
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
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class DefaultPersonaliaServiceTest {
    private static final String RIKTIG_IDENT = "56128349974";
    private static final String BARN_IDENT = "***REMOVED***";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";
    private static final Object BARN_SAMMENSATTNAVN = BARN_FORNAVN + " " + BARN_ETTERNAVN;

    private static final String FEIL_IDENT = "54321012345";
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

    private static final String NORGE = "Norge";
    private static final String NORGE_KODE = "NOR";

    @InjectMocks
    private DefaultPersonaliaService personaliaService;

    @Mock
    private PersonConnector personMock;

    @Mock
    @SuppressWarnings("PMD")
    private SoknadService soknadServiceMock;

    @Mock
    private BrukerprofilPortType brukerProfilMock;

    @Mock
    private Kodeverk kodeverkMock;

    //TODO Refaktorer tester og legg til de resterende testene fra PersonServiceTest

    private XMLBruker xmlBruker;

    @Before
    public void setup() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getPoststed(EN_ANNEN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getLand(NORGE_KODE)).thenReturn(NORGE);

        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        Person person = genererPersonMedGyldigIdentOgNavn(RIKTIG_IDENT, ET_FORNAVN, ET_ETTERNAVN);
        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN);

        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();

        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        familieRelasjoner.add(familierelasjon);
        response.setPerson(person);
        when(personMock.hentKjerneinformasjon(org.mockito.Matchers.any(HentKjerneinformasjonRequest.class))).thenReturn(response);

        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlBruker = new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        preferanserResponse.setPerson(xmlBruker);
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(org.mockito.Matchers.any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(preferanserResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnerPersonaliaUtenDataHvisPersonenSomReturneresHarFeilIdent() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(FEIL_IDENT);
        when(personMock.hentKjerneinformasjon(request)).thenThrow(HentKjerneinformasjonPersonIkkeFunnet.class);
        Personalia personalia;
        try {
            personalia = personaliaService.hentPersonalia(FEIL_IDENT);
        } catch (IkkeFunnetException | WebServiceException
                | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet
                | HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            personalia = null;
        }
        assertThat(personalia, is(not(nullValue())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonaliaObjektDersomPersonenSomReturneresHarRiktigIdent() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPerson();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getFnr(), is(RIKTIG_IDENT));
        assertThat(personalia.getNavn(), is(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalHenteBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPersonMedBarn();

        personaliaService.lagrePersonaliaOgBarn(RIKTIG_IDENT, 21L);

        verify(soknadServiceMock, times(2)).lagreSystemFaktum(anyLong(), any(Faktum.class), anyString());
    }

    @Test
    public void skalStottePersonerUtenMellomnavn() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        mockGyldigPersonUtenMellomnavn();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getNavn(), is(ET_FORNAVN + " " + ET_ETTERNAVN));
    }

    @Test
    public void skalStottePersonerUtenNavn() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        mockGyldigPersonUtenNavn();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getNavn(), is(""));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonObjektMedAdresseInformasjon() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetGjeldendeAdresse = EN_ADRESSE_GATE + " " + EN_ADRESSE_HUSNUMMER + EN_ADRESSE_HUSBOKSTAV + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        String forventetSekunarAdresse = EN_POSTBOKS_ADRESSEEIER + ", " + EN_ANNEN_ADRESSE_GATE + " " + EN_ANNEN_ADRESSE_HUSNUMMER + EN_ANNEN_ADRESSE_HUSBOKSTAV + ", " + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        mockGyldigPersonMedAdresse();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        assertThat(personalia, is(not(nullValue())));

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        assertThat(gjeldendeAdresse, is(not(nullValue())));
        assertThat(sekundarAdresse, is(not(nullValue())));

        assertThat(gjeldendeAdresse.getAdressetype(), is(Adressetype.BOSTEDSADRESSE.name()));
        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));

        assertThat(sekundarAdresse.getAdressetype(), is(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE.name()));
        assertThat(sekundarAdresse.getAdresse(), is(forventetSekunarAdresse));
    }

    private void mockGyldigPersonMedAdresse() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);

        xmlBruker.setBostedsadresse(bostedsadresse);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    }

    private void mockGyldigPersonUtenNavn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        xmlBruker.setPersonnavn(personNavn);
    }

    private void mockGyldigPersonUtenMellomnavn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnUtenMellomnavn());
    }

    private void mockGyldigPerson() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void mockGyldigPersonMedBarn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
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

    private XMLHentKontaktinformasjonOgPreferanserRequest hentRequestMedGyldigIdent() {
        XMLHentKontaktinformasjonOgPreferanserRequest request = new XMLHentKontaktinformasjonOgPreferanserRequest();
        request.setIdent(RIKTIG_IDENT);
        return request;
    }

//    private Person skalStotteMidlertidigUtenlandskMidlertidigAdresser(int antallAdresseLinjer) throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
//        XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
//        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
//
//        XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
//
//        XMLMidlertidigPostadresseUtland midlertidigPostboksAdresseUtlandet = generateMidlertidigAdresseUtlandet(antallAdresseLinjer);
//        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseUtlandet);
//
//        response.setPerson(xmlBruker);
//
//        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
//        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);
//        Person hentetPerson = service.hentPerson(5l, RIKTIG_IDENT);
//
//        return hentetPerson;
//    }

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

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EN_EPOST);
    }

    private static XMLLandkoder lagLandkode() {
        return new XMLLandkoder().withValue(NORGE);
    }

    private static XMLPersonnavn navnMedMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setMellomnavn(ET_MELLOMNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
    }

    private static XMLPersonnavn navnUtenMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
    }

    private Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String etternavn) {
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
}
