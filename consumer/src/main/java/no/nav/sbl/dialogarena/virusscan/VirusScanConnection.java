package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static no.nav.sbl.dialogarena.virusscan.Result.OK;

class VirusScanConnection {

    private static final Logger logger = LoggerFactory.getLogger(VirusScanConnection.class);

    private final VirusScanConfig config;
    private final RestOperations operations;

    VirusScanConnection(VirusScanConfig config, RestOperations operations) {
        this.config = config;
        this.operations = operations;
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    boolean isInfected(String filnavn, byte[] data, String behandlingsId, String mimeType) {
        try {
            if (ServiceUtils.isNonProduction() && filnavn.startsWith("virustest")) {
                return true;
            }
            logger.info("Scanner {} bytes for mimetype {} (fra Tika)", data.length, mimeType);
            ScanResult[] scanResults = putForObject(config.getUri(), data);
            if (scanResults.length != 1) {
                logger.warn("Uventet respons med lengde {}, forventet lengde er 1", scanResults.length);
                return false;
            }
            ScanResult scanResult = scanResults[0];
            if (OK.equals(scanResult.getResult())) {
                logger.info("Ingen virus i fil");
                return false;
            }
            logger.warn("Fant virus i fil for behandlingsId {}, status {}", behandlingsId, scanResult.getResult());
            return true;
        } catch (Exception e) {
            logger.warn("Kunne ikke scanne fil for behandlingsId {}", behandlingsId, e);
            return false;
        }
    }

    private ScanResult[] putForObject(URI uri, Object payload) {
        return operations.exchange(RequestEntity.put(uri).body(payload), ScanResult[].class).getBody();
    }
}
