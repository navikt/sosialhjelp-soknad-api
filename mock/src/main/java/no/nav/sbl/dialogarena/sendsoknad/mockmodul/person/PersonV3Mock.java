package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonV3Mock {

    private static final Logger log = LoggerFactory.getLogger(PersonV3Mock.class);

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

            Bruker defaultPerson = getDefaultPerson();
            Integer husnummer;
            try {
                husnummer = Integer.valueOf(husnr);
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

            responses.put(OidcFeatureToggleUtils.getUserId(), defaultPerson);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public PersonV3 PersonV3Mock() {

        PersonV3 mock = mock(PersonV3.class);

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenAnswer((invocationOnMock) -> createPersonV3HentPersonRequest(OidcFeatureToggleUtils.getUserId()));
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            hentPersonSikkerhetsbegrensning.printStackTrace();
        }

        return mock;
    }


    public static HentPersonResponse createPersonV3HentPersonRequest(String userId) {
        HentPersonResponse response = new HentPersonResponse();
        response.setPerson(responses.getOrDefault(userId, getDefaultPerson()));
        log.info("PersonV3Mock - har adresse??");
        if (response.getPerson().getBostedsadresse() != null) {
            log.info("PersonV3Mock - bostedsadresse: " + response.getPerson().getBostedsadresse());
            if (response.getPerson().getBostedsadresse().getStrukturertAdresse() != null) {
                log.info("PersonV3Mock - bostedsadresse.strukturertadresse: " + response.getPerson().getBostedsadresse().getStrukturertAdresse());
                if (((Gateadresse) response.getPerson().getBostedsadresse().getStrukturertAdresse()).getPoststed() != null) {
                    log.info("PersonV3Mock - bostedsadresse.strukturertadresse.poststed: " + ((Gateadresse) response.getPerson().getBostedsadresse().getStrukturertAdresse()).getPoststed());
                }
            }
        }
        return response;
    }

    public static Bruker getDefaultPerson() {
        Bruker person = genererPersonMedGyldigIdentOgNavn("01234567890", "Donald", "D.", "Mockmann");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        return person;
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
