package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.aetat.arena.personstatus.PersonstatusType;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
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
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            final Map<String, WSHentSoknadResponse> lager = new HashMap<>();
            SendSoknadPortType mock = new SendSoknadPortType() {
                @Override
                public void ping() {

                }

                @Override
                public WSEmpty sendSoknad(WSSoknadsdata parameters) {
                    return null;
                }

                @Override
                public WSEmpty mellomlagreSoknad(WSSoknadsdata parameters) {

                    return new WSEmpty();
                }

                @Override
                public WSHentSoknadResponse hentSoknad(WSBehandlingsId parameters) {
                    return lager.get(parameters.getBehandlingsId());
                }

                @Override
                public void avbrytSoknad(String behandlingsId) {

                }

                @Override
                public WSBehandlingsId startSoknad(WSStartSoknadRequest parameters) {
                    String uuid = UUID.randomUUID().toString();
                    lager.put(uuid, new WSHentSoknadResponse().withBehandlingsId(uuid).withAny(parameters.getAny()));
                    return new WSBehandlingsId().withBehandlingsId(uuid);
                }

                @Override
                public List<WSBehandlingskjedeElement> hentBehandlingskjede(String behandlingsId) {
                    return null;
                }
            };
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
            FilLagerPortType filLagerPortType = new FilLagerPortType() {
                @Override
                public void slett(String s) {

                }

                @Override
                public void ping() {

                }

                @Override
                public void slettAlle(String s) {

                }

                @Override
                public void lagre(String s, String s2, String s3, DataHandler dataHandler) {
                    InputStream inputStream = null;
                    OutputStream os = null;
                    File file;
                    try {
                        file = new File("C:" + File.separator + "temp" + File.separator + s2);
//                        file = new File("C:" + File.separator + "temp" + File.separator + "ettersending.pdf");
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        inputStream = dataHandler.getInputStream();
                        os = new FileOutputStream(file);
                        IOUtils.copy(inputStream, os);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

                @Override
                public List<WSInnhold> hentAlle(String s) {
                    return null;
                }

                @Override
                public void hent(Holder<String> stringHolder, Holder<DataHandler> dataHandlerHolder) {
                    try {
                        File file = new File("C:" + File.separator + "temp" + File.separator + stringHolder.value);
                        InputStream in = new FileInputStream(file);
                        dataHandlerHolder.value = new DataHandler(new ByteArrayDataSource(in, "application/octet-stream"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
            return filLagerPortType;
        }

        @Bean
        public FilLagerPortType fillagerServiceSelftest() {
            return fillagerService();
        }
    }

    @Configuration
    public static class PersonInfoWSConfig {
        public static final String ARBS = "ARBS";

        @Bean
        public PersonInfoServiceSoap personInfoServiceSoap() {
            PersonInfoServiceSoap mock = mock(PersonInfoServiceSoap.class);
            Personstatus personstatus = new Personstatus();
            PersonstatusType.PersonData personData = new PersonstatusType.PersonData();
            personData.setStatusArbeidsoker(ARBS);
            personstatus.setPersonData(personData);

            try {
                when(mock.hentPersonStatus(any(Fodselsnr.class))).thenReturn(personstatus);
            } catch (FaultGeneriskMsg faultGeneriskMsg) {
                return mock;
            }

            return mock;
        }
    }

    @Configuration
    public static class PersonWSConfig {

        @Bean
        public PersonPortType personService() throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
            PersonPortType mock = mock(PersonPortType.class);
            HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
            Person person = genererPersonMedGyldigIdentOgNavn("03076321565", "person", "mock");

            Statsborgerskap statsborgerskap = new Statsborgerskap();
            Landkoder landkoder = new Landkoder();
            landkoder.setValue("NOR");
            statsborgerskap.setLand(landkoder);
            person.setStatsborgerskap(statsborgerskap);

            List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();

            Familierelasjon familierelasjon = new Familierelasjon();
            Person barn1 = genererPersonMedGyldigIdentOgNavn("01010091736", "Dole", "Mockmann");
            familierelasjon.setTilPerson(barn1);
            Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
            familieRelasjonRolle.setValue("BARN");
            familierelasjon.setTilRolle(familieRelasjonRolle);
            familieRelasjoner.add(familierelasjon);

            Familierelasjon familierelasjon2 = new Familierelasjon();
            Person barn2 = genererPersonMedGyldigIdentOgNavn("03060193877", "Ole", "Mockmann");
            Doedsdato doedsdato = new Doedsdato();
            doedsdato.setDoedsdato(XMLGregorianCalendarImpl.createDate(2014, 2, 2, 0));
            barn2.setDoedsdato(doedsdato);
            familierelasjon2.setTilPerson(barn2);
            Familierelasjoner familieRelasjonRolle2 = new Familierelasjoner();
            familieRelasjonRolle2.setValue("BARN");
            familierelasjon2.setTilRolle(familieRelasjonRolle2);
            familieRelasjoner.add(familierelasjon2);

            Familierelasjon familierelasjon3 = new Familierelasjon();
            Person barn3 = genererPersonMedGyldigIdentOgNavn("03060194075", "Doffen", "Mockmann");
            familierelasjon3.setTilPerson(barn3);
            Familierelasjoner familieRelasjonRolle3 = new Familierelasjoner();
            familieRelasjonRolle3.setValue("BARN");
            familierelasjon3.setTilRolle(familieRelasjonRolle3);
            familieRelasjoner.add(familierelasjon3);

            response.setPerson(person);

            when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenReturn(response);
            //Mockito.doThrow(new RuntimeException()).when(mock).hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class));
            //when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class))).thenThrow(new WebServiceException());

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
    public static class KodeverkWSConfig {
        private static XMLHentKodeverkResponse kodeverkResponse() {
            XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo"));
            XMLKode kode2 = new XMLKode().withNavn("0560").withTerm(new XMLTerm().withNavn("Oslo"));

            XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge"));
            XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige"));
            XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania"));
            XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark"));
            XMLKode finland = new XMLKode().withNavn("FIN").withTerm(new XMLTerm().withNavn("Finland"));

            return new XMLHentKodeverkResponse()
                    .withKodeverk(new XMLEnkeltKodeverk()
                            .withNavn("Postnummer")
                            .withKode(kode, kode2)
                            .withNavn("Landkoder")
                            .withKode(norge, sverige, albania, danmark, finland));
        }

        @Bean
        public KodeverkPortType kodeverkService() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            KodeverkPortType mock = mock(KodeverkPortType.class);
            when(mock.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(kodeverkResponse());

            return mock;
        }

        @Bean
        public KodeverkPortType kodeverkServiceSelftest() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            return kodeverkService();
        }

    }

    @Configuration
    public static class BrukerProfilWSConfig {
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

        @Bean
        public BrukerprofilPortType brukerProfilService() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
            BrukerprofilPortType mock = mock(BrukerprofilPortType.class);
            XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
            XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);

            settAdresse(xmlBruker, "BOSTEDSADRESSE");
            settSekundarAdresse(xmlBruker);

            response.setPerson(xmlBruker);

            when(mock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(response);
            //Mockito.doThrow(new RuntimeException()).when(mock).hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class));

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

            return xmlBruker;
        }

        public BrukerprofilPortType brukerProfilSelftest() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
            return brukerProfilService();
        }
    }
}
