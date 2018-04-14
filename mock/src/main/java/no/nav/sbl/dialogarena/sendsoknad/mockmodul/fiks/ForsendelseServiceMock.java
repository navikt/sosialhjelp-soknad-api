package no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks;

import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import org.slf4j.Logger;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

public class ForsendelseServiceMock {
    private static final Logger logger = getLogger(ForsendelseServiceMock.class);


    public ForsendelsesServiceV9 forsendelseMock() {
        ForsendelsesServiceV9 amock = mock(ForsendelsesServiceV9.class);

        try {
            when(amock.sendForsendelse(any())).then(invocation -> {
                Forsendelse forsendelse = invocation.getArgumentAt(0, Forsendelse.class);
                String behId = forsendelse.getEksternref();

                if (!isEmpty(forsendelse.getSvarPaForsendelse())) {
                    logger.info("Sendt til Mock-Fiks som ettersendelse på {}", forsendelse.getSvarPaForsendelse());
                }

                String fiksId = "fake-fiksid-" + behId;
                logger.info("Mocker kall til Fiks, returnerer fake-fiksid: {}", fiksId);
                return fiksId;
            });
        } catch (Exception ignored) {
        }
        return amock;
    }

}
