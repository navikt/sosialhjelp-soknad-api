package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.aktoer.v1.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v1.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ComponentScan(excludeFilters = @Filter(Configuration.class))

public class MockConsumerConfig {

    @Configuration
    public static class SendSoknadWSConfig {
        
        @Bean
        public SendSoknadPortType sendSoknadService() {
            SendSoknadPortType mock = mock(SendSoknadPortType.class);
            when(mock.startSoknad(any(WSStartSoknadRequest.class))).thenAnswer(new Answer<WSBehandlingsId>(){
                @Override
                public WSBehandlingsId answer(InvocationOnMock invocation) throws Throwable {
                    return new WSBehandlingsId().withBehandlingsId(UUID.randomUUID().toString());
                }
                
            });
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
            return new AktoerPortType() {
                @Override
                public HentAktoerIdForIdentResponse hentAktoerIdForIdent(HentAktoerIdForIdentRequest request) throws HentAktoerIdForIdentPersonIkkeFunnet {
                    HentAktoerIdForIdentResponse hentAktoerIdForIdentResponse = new HentAktoerIdForIdentResponse();
                    hentAktoerIdForIdentResponse.setAktoerId("***REMOVED***");
                    return hentAktoerIdForIdentResponse;
                }

                @Override
                public void ping() {

                }
            };
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
          
            when(mock.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(postnummerKodeverkResponse());
            when(mock.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());
    
            return mock;
        }
        
        @Bean
        public KodeverkPortType kodeverkServiceSelftest() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            return kodeverkService();
        }

        private static XMLHentKodeverkResponse postnummerKodeverkResponse() {
            XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo"));
            XMLKode kode2 = new XMLKode().withNavn("0560").withTerm(new XMLTerm().withNavn("Oslo"));
            return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Postnummer").withKode(kode,kode2));
        }

    }

    @Configuration
    public static class BrukerProfilWSConfig {
        private static final String RIKTIG_IDENT = "12345612345";
        private static final String ET_FORNAVN = "Ola";
        private static final String ET_MELLOMNAVN = "Johan";
        private static final String ET_ETTERNAVN = "Mockmann";

        private static final String EN_EPOST = "test@epost.com";
        private static final String EN_ADRESSE_GATE = "Grepalida";
        private static final String EN_ADRESSE_HUSNUMMER = "44";
        private static final String EN_ADRESSE_HUSBOKSTAV = "B";
        private static final String EN_ADRESSE_POSTNUMMER = "0560";

        private static final String EN_ADRESSELINJE = "Poitigatan 55";
        private static final String EN_ANNEN_ADRESSELINJE = "Nord-Poiti";
        private static final String EN_TREDJE_ADRESSELINJE = "1111";
        private static final String EN_FJERDE_ADRESSELINJE = "Helsinki";
        
        @Bean
        public BrukerprofilPortType brukerProfilService() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
            BrukerprofilPortType mock = mock(BrukerprofilPortType.class);
            XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
            XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

            settAdresse(xmlBruker, "UTENLANDSK_ADRESSE");

            response.setPerson(xmlBruker);

            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(response);

            return mock;
        }

        private void settAdresse(XMLBruker xmlBruker, String type) {
            if("BOSTEDSADRESSE".equals(type)) {
                XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
                xmlBruker.setBostedsadresse(bostedsadresse);
                
                XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
                postadressetyper.setValue("BOSTEDSADRESSE");
                xmlBruker.setGjeldendePostadresseType(postadressetyper);
            } else if("UTENLANDSK_ADRESSE".equals(type)) {
                XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
                XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(4);

                XMLLandkoder xmlLandkode = new XMLLandkoder();
                xmlLandkode.setValue("FIN");
                utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

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

            return xmlBruker;
        }

        private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
            return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
        }

        private static XMLElektroniskAdresse lagElektroniskAdresse() {
            return new XMLEPost().withIdentifikator(EN_EPOST);
        }

        public BrukerprofilPortType brukerProfilSelftest() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
            return brukerProfilService();
        }
    }
}
