package no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.mockito.Mockito;

public class KodeverkMock {

    public KodeverkPortType kodeverkMock() {
        return Mockito.mock(KodeverkPortType.class);
    }

}