package no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk;

import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;

import static org.mockito.Mockito.mock;

public class PersonMock {

    public PersonPortType personMock() {
        return mock(PersonPortType.class);
    }

}
