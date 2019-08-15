package no.nav.sbl.dialogarena.virusscan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ClamAvVirusScanner implements VirusScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ClamAvVirusScanner.class);
    private final VirusScanConnection connection;

    @Inject
    public ClamAvVirusScanner(VirusScanConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean scan(String filnavn, byte[] data) {
        if (connection.isEnabled()) {
            return connection.scan(filnavn, data);
        }
        LOG.info("Virusscanning er ikke aktivert");
        return true;
    }
}
