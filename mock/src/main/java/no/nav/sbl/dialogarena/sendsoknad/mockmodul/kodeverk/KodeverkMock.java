package no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;

import static org.mockito.Mockito.mock;

public class KodeverkMock {

    public KodeverkPortType kodeverkMock() {
        return mock(KodeverkPortType.class);
    }

}