package no.nav.sosialhjelp.soknad.mock.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;

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
            String kontonummer = node.at("/person/bankkonto/bankkonto/bankkontonummer").textValue();

            Bruker defaultPerson = getDefaultPerson();
            defaultPerson.setBankkonto(new BankkontoNorge().withBankkonto(new Bankkontonummer().withBankkontonummer(kontonummer)));

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
        response.setPerson(responses.getOrDefault(userId, getDefaultPerson()));
        return response;
    }

    public static Bruker getDefaultPerson() {
        return new Bruker();
    }
}
