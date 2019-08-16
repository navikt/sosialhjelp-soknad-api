package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

@Service
public class ClamAvVirusScanner implements VirusScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ClamAvVirusScanner.class);
    private final VirusScanConnection connection;

    @Inject
    public ClamAvVirusScanner(VirusScanConfig config) {
        this.connection = new VirusScanConnection(config, new RestTemplate());
    }

    @Override
    public boolean scan(String filnavn, byte[] data) throws OpplastingException {
        if (connection.isEnabled() && connection.isInfected(filnavn, data)) {
            throw new OpplastingException("Fant virus i " + filnavn, null, "vedlegg.opplasting.feil.muligVirus");
        } else if (!connection.isEnabled()) {
            LOG.info("Virusscanning er ikke aktivert");
        }
        return true;
    }
}
