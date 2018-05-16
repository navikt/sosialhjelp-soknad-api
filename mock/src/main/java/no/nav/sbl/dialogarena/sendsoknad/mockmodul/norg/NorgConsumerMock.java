package no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.RsNorgEnhet;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

public class NorgConsumerMock {
    private static final Logger logger = getLogger(NorgConsumerMock.class);

    public NorgConsumer norgMock() {
        NorgConsumer mock = mock(NorgConsumer.class);

        when(mock.finnEnhetForGeografiskTilknytning(anyString())).thenAnswer(invocation -> {
            String param = invocation.getArgumentAt(0, String.class);

            logger.info("Henter norg enhet for gt {}", param);

            RsNorgEnhet e = new RsNorgEnhet();
            e.enhetId = 123;
            e.navn = "NAV Gamle Oslo";
            e.orgNrTilKommunaltNavKontor = "123456789";
            return e;
        });

        return mock;
    }
}
