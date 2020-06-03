package no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks;

import no.ks.svarut.servicesv9.Dokument;
import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.OrganisasjonDigitalAdresse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

public class ForsendelseServiceMock {
    private static final Logger logger = getLogger(ForsendelseServiceMock.class);


    public ForsendelsesServiceV9 forsendelseMock() {
        ForsendelsesServiceV9 amock = mock(ForsendelsesServiceV9.class);

        try {
            when(amock.sendForsendelse(any())).then(invocation -> {
                Forsendelse forsendelse = invocation.getArgument(0);
                String behId = forsendelse.getEksternref();

                String mottakerOrg = ((OrganisasjonDigitalAdresse) forsendelse.getMottaker().getDigitalAdresse()).getOrgnr();
                String mottakerNavn = forsendelse.getMottaker().getPostAdresse().getNavn();

                String fiksId = "fake-fiksid-" + behId;
                logger.info("Mocker kall til Fiks, sendt til {}/{}, returnerer fake-fiksid: {}", mottakerNavn, mottakerOrg , fiksId);

                if (!isEmpty(forsendelse.getSvarPaForsendelse())) {
                    logger.info("Sendt til Mock-Fiks som ettersendelse på {}", forsendelse.getSvarPaForsendelse());
                }

                if ("true".equals(System.getProperty("start.fiks.withmock.writetofile"))) {
                    lagreForsendelsesfilerLokalt(forsendelse, fiksId);
                }

                return fiksId;
            });
        } catch (Exception ignored) {
        }
        return amock;
    }

    private void lagreForsendelsesfilerLokalt(Forsendelse forsendelse, String fiksId) {
        String mappenavn = "mock/target/" + fiksId;
        File mappeForForsendelse = new File(mappenavn);
        mappeForForsendelse.mkdir();
        logger.info("Lagrer filer lokalt i " + mappenavn);
        for (Dokument dokument : forsendelse.getDokumenter()) {
            OutputStream os;
            InputStream is;
            try {
                is = dokument.getData().getInputStream();
                os = new FileOutputStream(new File(mappenavn + "/" + dokument.getFilnavn()));
                IOUtils.copy(is, os);
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
