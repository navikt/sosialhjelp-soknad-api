package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;

import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.hamcrest.CustomMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@ComponentScan(excludeFilters = @Filter(Configuration.class))

public class MockConsumerConfig {

    @Configuration
    public static class SendSoknadWSConfig {
        private int id = 0;

        @Bean
        public SendSoknadPortType sendSoknadService() {
            SendSoknadPortType mock = mock(SendSoknadPortType.class);
            when(mock.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(new WSBehandlingsId().withBehandlingsId("ID" + id++));
            return mock;
        }

        @Bean
        public SendSoknadPortType sendSoknadSelftest() {
            return sendSoknadService();
        }
    }

    @Configuration
    public static class FilLagerWSConfig {

        @Bean
        public FilLagerPortType fillagerService() {
            return mock(FilLagerPortType.class);
        }

        @Bean
        public FilLagerPortType fillagerServiceSelftest() {
            return fillagerService();
        }
    }

    @Configuration
    public static class PersonWSConfig {

        @Bean
        public PersonPortType personService() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
            PersonPortType mock = mock(PersonPortType.class);
            HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
            Person person = genererPersonMedGyldigIdentOgNavn("***REMOVED***", "person", "mock");
            List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();
            Familierelasjon familierelasjon = new Familierelasjon();
            Person barn1 = genererPersonMedGyldigIdentOgNavn("***REMOVED***", "Barn1", "mock");
            familierelasjon.setTilPerson(barn1);
            Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
            familieRelasjonRolle.setValue("FARA");
            familierelasjon.setTilRolle(familieRelasjonRolle);
            familieRelasjoner.add(familierelasjon);
            response.setPerson(person);
            when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenReturn(response);
            return mock;
        }

        @Bean
        public PersonPortType personServiceSelftest() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
            return personService();
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

    @Configuration
    public static class AktorWsConfig {

        @Bean
        public AktoerPortType aktorPortType() {
            return mock(AktoerPortType.class);
        }

        @Bean
        public AktoerPortType aktorSelftestPortType() {
            return aktorPortType();
        }
    }

    @Configuration
    public static class KodeverkWSConfig {

        private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
            XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge"));
            XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige"));
            XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania"));
            XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark"));

            return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, sverige, albania, danmark));
        }

        @Bean
        public KodeverkPortType kodeverkService() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            KodeverkPortType mock = mock(KodeverkPortType.class);
            when(mock.hentKodeverk(argThat(new CustomMatcher<XMLHentKodeverkRequest>("sjekk om kodeverk matcher") {
                @Override
                public boolean matches(Object item) {
                    XMLHentKodeverkRequest kodeverkRequest = (XMLHentKodeverkRequest) item;
                    return kodeverkRequest.getNavn().equals("Landkoder");
                }
            }))).thenReturn(postnummerKodeverkResponse());
            return mock;
        }

        @Bean
        public KodeverkPortType kodeverkServiceSelftest() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            return kodeverkService();
        }

        private XMLHentKodeverkResponse postnummerKodeverkResponse() {
            XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo"));
            return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
        }

    }

    @Configuration
    public static class BrukerProfilWSConfig {
        
        
        @Bean
        public BrukerprofilPortType brukerProfilService() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
            BrukerprofilPortType mock = mock(BrukerprofilPortType.class);
            
            XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse(); 
            XMLBruker person = genererXmlBrukerMedGyldigIdentOgNavn();
            person.setPostadresse(genererUtenlandskPostadresse());
            XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
            postadressetyper.setValue("POSTADRESSE");
            person.setGjeldendePostadresseType(postadressetyper);
            response.setPerson(person);
            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(response);
            return mock;
        }
        
        private XMLPostadresse genererUtenlandskPostadresse() {
            XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
            XMLUstrukturertAdresse ustrukturertAdresse =  new XMLUstrukturertAdresse();
            ustrukturertAdresse.setAdresselinje1("Poitigatan 55");
            ustrukturertAdresse.setAdresselinje2("Nord-Poiti");
            ustrukturertAdresse.setAdresselinje3("1111");
            ustrukturertAdresse.setAdresselinje4("Helsinki");
            XMLLandkoder xmlLandkode = new XMLLandkoder();
            xmlLandkode.setValue("POL");
            ustrukturertAdresse.setLandkode(xmlLandkode);

            xmlPostadresseUtland.setUstrukturertAdresse(ustrukturertAdresse);
            
            return xmlPostadresseUtland;
        }

        private XMLBruker genererXmlBrukerMedGyldigIdentOgNavn() {
            //XMLBruker xmlBruker = new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
            XMLBruker xmlBruker = new XMLBruker();
            XMLPersonnavn personNavn = new XMLPersonnavn();
            personNavn.setFornavn("Jan");
            personNavn.setMellomnavn("P.");
            personNavn.setEtternavn("Mock");
            personNavn.setSammensattNavn("Jan P. Mock");
            xmlBruker.setPersonnavn(personNavn);
            XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
            xmlNorskIdent.setIdent("***REMOVED***");
            xmlBruker.setIdent(xmlNorskIdent);
            return xmlBruker;
        }


        @Bean
        public BrukerprofilPortType brukerProfilSelftest() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
            return brukerProfilService();
        }
    }
}
