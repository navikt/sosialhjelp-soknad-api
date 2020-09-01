package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV3Mock {

    private static HashMap<String, Person> responses = new HashMap<>();

    public static void setPersonV3(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.readTree(json);
            String gatenavnNode = node.at("/person/bostedsadresse/strukturertAdresse/gatenavn").textValue();
            String postkodeNode = node.at("/person/bostedsadresse/strukturertAdresse/poststed/value").textValue();
            String kommunenr = node.at("/person/bostedsadresse/strukturertAdresse/kommunenummer").textValue();
            String husnr = node.at("/person/bostedsadresse/strukturertAdresse/husnummer").textValue();
            String kontonummer = node.at("/person/bankkonto/bankkonto/bankkontonummer").textValue();
            String statborgerskap = node.at("/person/statsborgerskap/landkode/value").textValue();

            String midlertidigAdresseGnr = node.at("/person/midlertidigPostadresse/strukturertAdresse/gnr").textValue();
            String midlertidigAdresseBnr = node.at("/person/midlertidigPostadresse/strukturertAdresse/bnr").textValue();
            String midlertidigAdresseKommunenummer = node.at("/person/midlertidigPostadresse/strukturertAdresse/kommunenummer").textValue();

            Bruker defaultPerson = getDefaultPerson(false);
            int husnummer;
            try {
                husnummer = Integer.parseInt(husnr);
            } catch (NumberFormatException e) {
                husnummer = 0;
            }

            defaultPerson.setBostedsadresse(new Bostedsadresse().withStrukturertAdresse(new Gateadresse()
                            .withGatenavn(gatenavnNode)
                            .withPoststed(new Postnummer().withValue(postkodeNode))
                            .withKommunenummer(kommunenr)
                            .withHusnummer(husnummer)
                    )
            );
            defaultPerson.setBankkonto(new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(kontonummer)));
            defaultPerson.setStatsborgerskap(new Statsborgerskap().withLand(new Landkoder().withValue(statborgerskap)));

            Matrikkeladresse midlertidigMatrikkeladresse = (Matrikkeladresse) ((MidlertidigPostadresseNorge) defaultPerson.getMidlertidigPostadresse()).getStrukturertAdresse();
            midlertidigMatrikkeladresse.withMatrikkelnummer(new Matrikkelnummer()
                    .withGaardsnummer(midlertidigAdresseGnr)
                    .withBruksnummer(midlertidigAdresseBnr));
            midlertidigMatrikkeladresse.withKommunenummer(midlertidigAdresseKommunenummer);


            responses.put(SubjectHandler.getUserId(), defaultPerson);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public PersonV3 personV3Mock() {
        PersonV3 mock = mock(PersonV3.class);

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenAnswer((invocationOnMock) -> createPersonV3HentPersonRequest(SubjectHandler.getUserId()));
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        }

        return mock;
    }

    public static HentPersonResponse createPersonV3HentPersonRequest(String userId) {
        HentPersonResponse response = new HentPersonResponse();
        response.setPerson(responses.getOrDefault(userId, getDefaultPerson(false)));
        return response;
    }

    public static HentPersonResponse createPersonV3HentPersonRequestForIntegrationTest(String userId) {
        HentPersonResponse response = new HentPersonResponse();
        response.setPerson(responses.getOrDefault(userId, getDefaultPerson(true)));
        return response;
    }

    public static Bruker getDefaultPerson(boolean forIntegrationTest) {
        Bruker person = genererPersonMedGyldigIdentOgNavn("01234567890", "Donald", "D.", "Mockmann");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);
        if (!forIntegrationTest) {
            person.withBostedsadresse(new Bostedsadresse().withStrukturertAdresse(createSandeiMoreOgRomsdalMatrikkelAdresse()));
            person.withMidlertidigPostadresse(new MidlertidigPostadresseNorge().withStrukturertAdresse(createOsloMatrikkelAdresse()));
        }

        return person;
    }

    private static Gateadresse createDobbelGateadresse() {
        return new Gateadresse()
                .withKommunenummer("0301")
                .withPoststed(new Postnummer().withValue("2222"))
                .withGatenavn("Dobbelveien")
                .withBolignummer("2")
                .withHusnummer(3);
    }

    private static Matrikkeladresse createOsloMatrikkelAdresse() {
        return new Matrikkeladresse()
                .withKommunenummer("0301")
                .withEiendomsnavn("SLOTTSSTALLEN")
                .withPoststed(new Postnummer().withValue("0010"))
                .withMatrikkelnummer(new Matrikkelnummer()
                        .withGaardsnummer("209")
                        .withBruksnummer("25"));
    }

    private static Matrikkeladresse createSarpsborgMatrikkelAdresse() {
        return new Matrikkeladresse()
                .withKommunenummer("3003")
                .withEiendomsnavn("Sarpsborg Rådhus")
                .withPoststed(new Postnummer().withValue("1706"))
                .withMatrikkelnummer(new Matrikkelnummer()
                        .withGaardsnummer("1")
                        .withBruksnummer("174"));
    }

    private static Matrikkeladresse createSandeiMoreOgRomsdalMatrikkelAdresse() {
        return new Matrikkeladresse()
                .withKommunenummer("1514")
                .withEiendomsnavn("SandeHus")
                .withPoststed(new Postnummer().withValue("1706"))
                .withMatrikkelnummer(new Matrikkelnummer()
                        .withGaardsnummer("555")
                        .withBruksnummer("309"));
    }


    private static Bruker genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String mellomnavn, String etternavn) {
        Bruker xmlPerson = new Bruker();

        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        personnavn.setMellomnavn(mellomnavn);
        personnavn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(personnavn);

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(ident);
        norskIdent.setType(personidenter);

        return xmlPerson;
    }

    private static Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(lagDatatypeFactory().newXMLGregorianCalendarDate(year, month, day, 0));
        return foedselsdato;
    }

    private static DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
