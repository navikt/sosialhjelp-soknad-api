package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

@Service
public class ClamAvVirusScanner implements VirusScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClamAvVirusScanner.class);
    private final VirusScanConnection connection;

    @Inject
    public ClamAvVirusScanner(VirusScanConfig config) {
        this.connection = new VirusScanConnection(config, new RestTemplate());
    }

    @Override
    public void scan(String filnavn, byte[] data, String behandlingsId, String mimeType) throws OpplastingException {
        if (connection.isEnabled() && connection.isInfected(filnavn, data, behandlingsId, mimeType)) {
            throw new OpplastingException(String.format("Fant virus i fil for behandlingsId %s", behandlingsId), null, "vedlegg.opplasting.feil.muligVirus");
        } else if (!connection.isEnabled()) {
            logger.info("Virusscanning er ikke aktivert");
        }
    }
}
