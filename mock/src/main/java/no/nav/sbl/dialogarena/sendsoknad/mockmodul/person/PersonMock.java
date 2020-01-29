package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;

import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonMock {

    private static Map<String, HentKjerneinformasjonResponse> responses = new HashMap<>();

    public PersonPortType personMock() {

        PersonPortType mock = mock(PersonPortType.class);

        return getPersonPortType(mock);
    }

    public PersonPortTypeMock personPortTypeMock() {

        PersonPortTypeMock mock = mock(PersonPortTypeMock.class);

        return getPersonPortType(mock);
    }

    private <T extends PersonPortType> T getPersonPortType(T mock) {
        try {
            when(mock.hentKjerneinformasjon(any(HentKjerneinformasjonRequest.class)))
                    .thenAnswer((invocationOnMock -> getOrCreateCurrentUserResponse()));
        } catch (HentKjerneinformasjonPersonIkkeFunnet |
                HentKjerneinformasjonSikkerhetsbegrensning hentKjerneinformasjonPersonIkkeFunnet) {
            hentKjerneinformasjonPersonIkkeFunnet.printStackTrace();
        }

        return mock;
    }

    private static HentKjerneinformasjonResponse getOrCreateCurrentUserResponse(){
        HentKjerneinformasjonResponse response = responses.get(SubjectHandler.getUserIdFromToken());
        if (response == null){
            response = createNewResponse();
            responses.put(SubjectHandler.getUserIdFromToken(), response);
        }

        return response;
    }

    private static HentKjerneinformasjonResponse createNewResponse(){
        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        response.setPerson(getDefaultPerson());

        return response;
    }

    public static Person getDefaultPerson(){
        Person person = genererPersonMedGyldigIdentOgNavn("***REMOVED***", "Donald", "D.", "Mockmann");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        return person;
    }

    private static Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String mellomnavn, String etternavn) {
        Person xmlPerson = new Person();

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
        xmlPerson.setIdent(norskIdent);

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
    
    public static void setPersonMedFamilieforhold(String jsonPerson){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Person person = mapper.readValue(jsonPerson, Person.class);
            HentKjerneinformasjonResponse response = getOrCreateCurrentUserResponse();
            response.setPerson(person);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetFamilieforhold(){
        Person person = getDefaultPerson();
        HentKjerneinformasjonResponse response = getOrCreateCurrentUserResponse();
        response.setPerson(person);
    }
}
