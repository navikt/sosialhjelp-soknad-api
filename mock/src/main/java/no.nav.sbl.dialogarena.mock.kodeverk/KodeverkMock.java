package no.nav.sbl.dialogarena.mock.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.mockito.Mockito;

public class KodeverkMock {

    public KodeverkPortType kodeverkMock() {
        return Mockito.mock(KodeverkPortType.class);
    }

}