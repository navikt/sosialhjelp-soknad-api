package no.nav.sbl.dialogarena.sendsoknad.mockmodul.personinfo;

import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;

import static org.mockito.Mockito.mock;

public class PersonInfoMock {

    public PersonInfoServiceSoap personInfoMock() {
        return mock(PersonInfoServiceSoap.class);
    }

}
