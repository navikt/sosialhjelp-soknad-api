package no.nav.sbl.dialogarena.soknadinnsending.business.person;

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

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EosBorgerService;
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
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPreferanser;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@RunWith(value = MockitoJUnitRunner.class)
public class DefaultPersonaliaServiceTest {
    private static final String RIKTIG_IDENT = "56128349974";
    private static final String BARN_IDENT = "01010091736";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";
    
    private static final String FEIL_IDENT = "54321012345";
    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
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
    private static final List<String> EN_ADRESSE_UTLANDET = Arrays
            .asList(EN_ADRESSELINJE);
    private static final List<String> EN_ANNEN_ADRESSE_UTLANDET = Arrays
            .asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE);
    private static final List<String> EN_TREDJE_ADRESSE_UTLANDET = Arrays
            .asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE,
                    EN_TREDJE_ADRESSELINJE);
    private static final List<String> EN_FJERDE_ADRESSE_UTLANDET = Arrays
            .asList(EN_ADRESSELINJE, EN_ANNEN_ADRESSELINJE,
                    EN_TREDJE_ADRESSELINJE, EN_FJERDE_ADRESSELINJE);
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
    private EosBorgerService eosBorgerService;

    @Mock
    private Kodeverk kodeverkMock;
    private XMLBruker xmlBruker;
    private Person person;
    private DateTimeFormatter dateTimeFormat;

    @Before
    public void setup()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(
                EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getPoststed(EN_ANNEN_ADRESSE_POSTNUMMER)).thenReturn(
                EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getLand(NORGE_KODE)).thenReturn(NORGE);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);
        when(eosBorgerService.getStatsborgeskapType(NORGE_KODE)).thenReturn(
                "Norsk");

        dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        person = genererPersonMedGyldigIdentOgNavn(RIKTIG_IDENT,
                ET_FORNAVN, ET_ETTERNAVN);
        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn(BARN_IDENT,
                BARN_FORNAVN, BARN_ETTERNAVN);

        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();

        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        familieRelasjoner.add(familierelasjon);
        response.setPerson(person);
        when(
                personMock.hentKjerneinformasjon(org.mockito.Matchers
                        .any(HentKjerneinformasjonRequest.class))).thenReturn(
                response);

        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlBruker = new XMLBruker()
                .withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        preferanserResponse.setPerson(xmlBruker);
        when(
                brukerProfilMock
                        .hentKontaktinformasjonOgPreferanser(org.mockito.Matchers
                                .any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenReturn(preferanserResponse);
    }
    
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void returnerPersonaliaUtenDataHvisPersonenSomReturneresHarFeilIdent()
            throws HentKjerneinformasjonPersonIkkeFunnet,
            HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(FEIL_IDENT);
        when(personMock.hentKjerneinformasjon(request)).thenThrow(
                HentKjerneinformasjonPersonIkkeFunnet.class);
        Personalia personalia;
        personalia = personaliaService.hentPersonalia(FEIL_IDENT);

        assertThat(personalia, is(not(nullValue())));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonaliaObjektDersomPersonenSomReturneresHarRiktigIdent()
            throws HentKjerneinformasjonPersonIkkeFunnet,
            HentKjerneinformasjonSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPerson();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getFnr(), is(RIKTIG_IDENT));
        assertThat(personalia.getNavn(), is(ET_FORNAVN + " " + ET_MELLOMNAVN
                + " " + ET_ETTERNAVN));
    }
    
    @Test
    public void returnererPersonObjektMedStatsborgerskapUtenEpostOgBarn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlBruker = new XMLBruker();
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(RIKTIG_IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        preferanserResponse.setPerson(xmlBruker);
        when(
                brukerProfilMock
                        .hentKontaktinformasjonOgPreferanser(org.mockito.Matchers
                                .any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenReturn(preferanserResponse);
        
        mockGyldigPerson();
        
        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("DNK");
        statsborgerskap.setLand(landkode);
        person.setStatsborgerskap(statsborgerskap);
        
        XMLPreferanser preferanser = new XMLPreferanser();
        XMLElektroniskKommunikasjonskanal elektroniskKommKanal = new XMLElektroniskKommunikasjonskanal();
        elektroniskKommKanal.setElektroniskAdresse(null);
        preferanser.setForetrukketElektroniskKommunikasjonskanal(elektroniskKommKanal);
        xmlBruker.setPreferanser(preferanser);
        
        List<Familierelasjon> familierelasjoner = person.getHarFraRolleI();
        familierelasjoner.clear();
        
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getFnr(), is(RIKTIG_IDENT));
        assertThat(personalia.getNavn(), is(ET_FORNAVN + " " + ET_MELLOMNAVN
                + " " + ET_ETTERNAVN));
        
        assertThat(personalia.getStatsborgerskap(), is("DNK"));
        assertThat(personalia.getEpost(), is(""));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalHenteBarn() throws HentKjerneinformasjonPersonIkkeFunnet,
            HentKjerneinformasjonSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPersonMedBarn();

        personaliaService.lagrePersonaliaOgBarn(RIKTIG_IDENT, 21L);

        verify(soknadServiceMock, times(2)).lagreSystemFaktum(anyLong(),
                any(Faktum.class), anyString());
    }

    @Test
    public void skalStottePersonerUtenMellomnavn()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        mockGyldigPersonUtenMellomnavn();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getNavn(), is(ET_FORNAVN + " " + ET_ETTERNAVN));
    }

    @Test
    public void skalStottePersonerUtenNavn()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        mockGyldigPersonUtenNavn();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        assertThat(personalia, is(not(nullValue())));
        assertThat(personalia.getNavn(), is(""));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void returnererPersonObjektMedAdresseInformasjon()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetGjeldendeAdresse = EN_ADRESSE_GATE + " "
                + EN_ADRESSE_HUSNUMMER + EN_ADRESSE_HUSBOKSTAV + ", "
                + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        String forventetSekunarAdresse = EN_POSTBOKS_ADRESSEEIER + ", "
                + EN_ANNEN_ADRESSE_GATE + " " + EN_ANNEN_ADRESSE_HUSNUMMER
                + EN_ANNEN_ADRESSE_HUSBOKSTAV + ", "
                + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        mockGyldigPersonMedAdresse();

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        assertThat(personalia, is(not(nullValue())));

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        assertThat(gjeldendeAdresse, is(not(nullValue())));
        assertThat(sekundarAdresse, is(not(nullValue())));
        assertThat(personalia.harNorskMidlertidigAdresse(), is(true));

        assertThat(gjeldendeAdresse.getAdressetype(),
                is(Adressetype.BOSTEDSADRESSE.name()));
        assertThat(gjeldendeAdresse.getAdresse(), is(forventetGjeldendeAdresse));

        assertThat(sekundarAdresse.getAdressetype(),
                is(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE.name()));
        assertThat(sekundarAdresse.getAdresse(), is(forventetSekunarAdresse));

        assertThat(sekundarAdresse.getGyldigFra(),
                is(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_FRA)));
        assertThat(sekundarAdresse.getGyldigTil(),
                is(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_TIL)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigOmrodeAdresseNorge()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetsekundarAdresse = EN_ADRESSE_POSTNUMMER + " "
                + EN_ADRESSE_POSTSTED + ", " + ET_EIEDOMSNAVN;
        mockGyldigPersonMedMidlertidigOmrodeAdresse();
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        
        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse);
        assertThat(gjeldendeAdresse.getAdresse(), is(forventetsekundarAdresse));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigPostboksAdresseNorge()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetgjeldendeAdresse = EN_POSTBOKS_ADRESSEEIER + ", " + EN_ANNEN_ADRESSE_POSTNUMMER + " "
                + EN_ADRESSE_POSTSTED + ", " + ET_POSTBOKS_NAVN + " " + EN_POSTBOKS_NUMMER;
        
        mockGyldigPersonMedMidlertidigPostboksAdresse();
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        
        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse);
        assertThat(gjeldendeAdresse.getAdresse(), is(forventetgjeldendeAdresse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteFolkeregistretUtenlandskAdresse()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetAdresse = EN_ADRESSELINJE + ", "
                + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", "
                + EN_FJERDE_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedUtenlandskFolkeregistrertAdresse(4);

        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse.getAdresse());
        
        Assert.assertFalse(personalia.harNorskMidlertidigAdresse());
        Assert.assertTrue(personalia.harUtenlandskFolkeregistrertAdresse());
        
        assertThat(gjeldendeAdresse.getAdresse(), is(forventetAdresse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed1Linjer()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetAdresse = EN_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(1);
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertThat(sekundarAdresse.getAdresse(), is(forventetAdresse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed2Linjer()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetAdresse = EN_ADRESSELINJE + ", "
                + EN_ANNEN_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(2);
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertThat(sekundarAdresse.getAdresse(), is(forventetAdresse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed3Linjer()
            throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        String forventetAdresse = EN_ADRESSELINJE + ", "
                + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", "
                + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(3);
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertThat(sekundarAdresse.getAdresse(), is(forventetAdresse));
    }
    
    @Test
    public void returnererTomPersonaliaVedTpsFeil() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(
                brukerProfilMock
                        .hentKontaktinformasjonOgPreferanser(org.mockito.Matchers
                                .any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenThrow(new WebServiceException());
        
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        
        assertThat(personalia, is(not(nullValue())));
        Assert.assertNull(personalia.getAlder());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getFnr());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getKjonn());
        Assert.assertNull(personalia.getNavn());
        
        Assert.assertFalse(personalia.harNorskMidlertidigAdresse());
        Assert.assertFalse(personalia.harUtenlandskAdresse());
    }
    
    @Test
    public void returnererTomPersonaliaVedManglendePerson() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(
                brukerProfilMock
                        .hentKontaktinformasjonOgPreferanser(org.mockito.Matchers
                                .any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenThrow(new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet());
        
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        
        assertThat(personalia, is(not(nullValue())));
        Assert.assertNull(personalia.getAlder());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getFnr());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getKjonn());
        Assert.assertNull(personalia.getNavn());
    }
    
    @Test
    public void returnererTomPersonaliaVedSikkerhetsbegrensing() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(
                brukerProfilMock
                        .hentKontaktinformasjonOgPreferanser(org.mockito.Matchers
                                .any(XMLHentKontaktinformasjonOgPreferanserRequest.class)))
                .thenThrow(new HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning());
        
        Personalia personalia = personaliaService.hentPersonalia(RIKTIG_IDENT);
        
        assertThat(personalia, is(not(nullValue())));
        Assert.assertNull(personalia.getAlder());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getFnr());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getKjonn());
        Assert.assertNull(personalia.getNavn());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void returnereTomPersonaliaVedWebserviceFeilIPersonTjeneste() throws HentKjerneinformasjonPersonIkkeFunnet,
            HentKjerneinformasjonSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(personMock.hentKjerneinformasjon(org.mockito.Matchers
                                .any(HentKjerneinformasjonRequest.class)))
                .thenThrow(new WebServiceException());

        Personalia personalia = personaliaService.lagrePersonaliaOgBarn(RIKTIG_IDENT, 21L);

        assertThat(personalia, is(not(nullValue())));
        Assert.assertNull(personalia.getAlder());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getFnr());
        Assert.assertNull(personalia.getEpost());
        Assert.assertNull(personalia.getKjonn());
        Assert.assertNull(personalia.getNavn());
    }
    
        
    private void mockGyldigPersonMedMidlertidigUtenlandskAdresse(
            int adresselinjer) {
        XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(adresselinjer);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_UTLAND");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        
        xmlPostadresseUtland
                .setUstrukturertAdresse(utenlandskUstrukturertAdresse);

        xmlBruker.setPostadresse(xmlPostadresseUtland);

    }
    
    private void mockGyldigPersonMedUtenlandskFolkeregistrertAdresse(int adresselinjer) {
        XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
        //no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland xmlPostadresseUtland = new no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland();
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

    private void mockGyldigPersonMedMidlertidigOmrodeAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigOmrodeAdresseNorge = generateMidlertidigOmrodeAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigOmrodeAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }
    
    private void mockGyldigPersonMedMidlertidigPostboksAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void mockGyldigPersonMedAdresse()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);

        xmlBruker.setBostedsadresse(bostedsadresse);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    }

    private void mockGyldigPersonUtenNavn()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        xmlBruker.setPersonnavn(personNavn);
    }

    private void mockGyldigPersonUtenMellomnavn()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnUtenMellomnavn());
    }

    private void mockGyldigPerson()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void mockGyldigPersonMedBarn()
            throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning,
            HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        
        List<Familierelasjon> familierelasjoner = person.getHarFraRolleI();
        Familierelasjon familieRelasjon = new Familierelasjon();
        Familierelasjoner type = new Familierelasjoner();
        type.setValue("BARN");
        familieRelasjon.setTilRolle(type);
        familieRelasjon.setTilPerson(hentMockBarn());
        familierelasjoner.add(familieRelasjon);
        
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private Person hentMockBarn() {
        Person barn = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn("Jan");
        navn.setEtternavn("Mockmann");
        barn.setPersonnavn(navn);
        NorskIdent ident = new NorskIdent();
        ident.setIdent("03076321565");
        barn.setIdent(ident);
        
        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("DNK");
        statsborgerskap.setLand(landkode);
        barn.setStatsborgerskap(statsborgerskap);
        return barn;
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
        return new XMLElektroniskKommunikasjonskanal()
                .withElektroniskAdresse(lagElektroniskAdresse());
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
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " "
                + ET_ETTERNAVN);
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

    private Person genererPersonMedGyldigIdentOgNavn(String ident,
            String fornavn, String etternavn) {
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
