package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class BarnBolkTest {


    private static final String RIKTIG_IDENT = "56128349974";
    private static final String BARN_IDENT = "***REMOVED***";
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";

    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
    private static final String EN_ADRESSE_POSTNUMMER = "0560";
    private static final String EN_ADRESSE_POSTSTED = "Oslo";

    private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";

    private static final String ET_LAND = "Finland";
    private static final String EN_LANDKODE = "FIN";
    private static final String EN_EPOST = "test@epost.com";

    private static final String NORGE = "Norge";
    private static final String NORGE_KODE = "NOR";

    @InjectMocks
    private PersonaliaBolk personaliaBolk;

    @InjectMocks
    private BarnBolk barnBolk;

    @Mock
    private PersonService personMock;

    @Mock
    @SuppressWarnings("PMD")
    private FaktaService faktaService;

    @Mock
    private BrukerprofilPortType brukerProfilMock;

    @Mock
    private Kodeverk kodeverkMock;
    private XMLBruker xmlBruker;
    private Person person;

    @Before
    public void setup() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getPoststed(EN_ANNEN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getLand(NORGE_KODE)).thenReturn(NORGE);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);

        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        person = genererPersonMedGyldigIdentOgNavn(RIKTIG_IDENT, ET_FORNAVN, ET_ETTERNAVN);
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
    public void skalHenteBarn() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPersonMedBarn();

        List<Faktum> faktums = barnBolk.genererSystemFakta(BARN_IDENT, 21L);
        assertThat(faktums.size(), equalTo(1));
        assertThat(faktums.get(0).getProperties().get("fnr"), equalTo(BARN_IDENT));
    }

    @Test
    public void skalIkkeViseDoedeBarn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        mockGyldigPersonMedBarn();
        leggTilDoedeBarn();

        List<Faktum> faktums = barnBolk.genererSystemFakta(BARN_IDENT, 21L);
        assertThat(faktums.size(), equalTo(1));
        assertThat(faktums.get(0).getProperties().get("fnr"), equalTo(BARN_IDENT));
    }

    private void mockGyldigPersonMedBarn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {

        List<Familierelasjon> familierelasjoner = person.getHarFraRolleI();
        Familierelasjon familieRelasjon = new Familierelasjon();
        Familierelasjoner type = new Familierelasjoner();
        type.setValue("BARN");
        familieRelasjon.setTilRolle(type);
        familieRelasjon.setTilPerson(hentMockBarn());
        familierelasjoner.add(familieRelasjon);

        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void leggTilDoedeBarn() {
        List<Familierelasjon> familierelasjoner = person.getHarFraRolleI();
        Familierelasjon familieRelasjon = new Familierelasjon();
        Familierelasjoner type = new Familierelasjoner();
        type.setValue("BARN");
        familieRelasjon.setTilRolle(type);
        familieRelasjon.setTilPerson(hentDoedtMockBarn());
        familierelasjoner.add(familieRelasjon);

        Familierelasjon familieRelasjon2 = new Familierelasjon();
        Familierelasjoner type2 = new Familierelasjoner();
        type2.setValue("BARN");
        familieRelasjon2.setTilRolle(type);
        familieRelasjon2.setTilPerson(hentDoedtMockBarnMedBostatus());
        familierelasjoner.add(familieRelasjon2);
    }

    private Person hentMockBarn() {
        Person barn = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn("Jan");
        navn.setEtternavn("Mockmann");
        barn.setPersonnavn(navn);
        NorskIdent ident = new NorskIdent();
        ident.setIdent("***REMOVED***");
        barn.setIdent(ident);

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("DNK");
        statsborgerskap.setLand(landkode);
        barn.setStatsborgerskap(statsborgerskap);
        return barn;
    }

    private Person hentDoedtMockBarn() {
        Person barn = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn("Jon");
        navn.setEtternavn("Mockmann");
        barn.setPersonnavn(navn);
        NorskIdent ident = new NorskIdent();
        ident.setIdent("***REMOVED***");
        barn.setIdent(ident);

        Doedsdato doedsdato = new Doedsdato();
        doedsdato.setDoedsdato(XMLGregorianCalendarImpl.createDate(2014, 2, 2, 0));
        barn.setDoedsdato(doedsdato);

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("NOR");
        statsborgerskap.setLand(landkode);
        barn.setStatsborgerskap(statsborgerskap);
        return barn;
    }

    private Person hentDoedtMockBarnMedBostatus() {
        Person barn = new Person();
        Personnavn navn = new Personnavn();
        navn.setFornavn("Jarle");
        navn.setEtternavn("Mockmann");
        barn.setPersonnavn(navn);
        NorskIdent ident = new NorskIdent();
        ident.setIdent("***REMOVED***");
        barn.setIdent(ident);

        Personstatuser personstatus = new Personstatuser();
        personstatus.setValue("DØD");
        Personstatus personstatusWrapper = new Personstatus();
        personstatusWrapper.setPersonstatus(personstatus);
        barn.setPersonstatus(personstatusWrapper);

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("NOR");
        statsborgerskap.setLand(landkode);
        barn.setStatsborgerskap(statsborgerskap);
        return barn;
    }


    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EN_EPOST);
    }


    private static XMLPersonnavn navnMedMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setMellomnavn(ET_MELLOMNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN);
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
