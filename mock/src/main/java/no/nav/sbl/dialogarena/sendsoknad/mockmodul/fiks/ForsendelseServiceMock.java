package no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ForsendelseServiceMock {
    public ForsendelsesServiceV9 forsendelseMock() {
        ForsendelsesServiceV9 amock = mock(ForsendelsesServiceV9.class);

        try {
            when(amock.sendForsendelse(any())).thenReturn("fake-fiksid-1234567890");
        } catch (Exception ignored) {
        }
        return amock;
    }

}
