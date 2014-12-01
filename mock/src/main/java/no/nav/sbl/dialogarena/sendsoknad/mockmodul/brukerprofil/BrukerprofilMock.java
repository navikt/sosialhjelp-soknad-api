package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;

import static org.mockito.Mockito.mock;

public class BrukerprofilMock {

    public BrukerprofilPortType brukerprofilMock() {
        return mock(BrukerprofilPortType.class);
    }

}
