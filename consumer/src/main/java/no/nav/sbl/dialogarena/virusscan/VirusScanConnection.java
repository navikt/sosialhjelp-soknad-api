package no.nav.sbl.dialogarena.virusscan;

import static no.nav.sbl.dialogarena.virusscan.Result.OK;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

class VirusScanConnection {

    private static final Logger LOG = LoggerFactory.getLogger(VirusScanConnection.class);
    private static final AtomicInteger INGENVIRUS_COUNTER = new AtomicInteger();
    private static final AtomicInteger VIRUS_COUNTER = new AtomicInteger();

    private final VirusScanConfig config;
    private final RestOperations operations;

    public VirusScanConnection(VirusScanConfig config, RestOperations operations) {
        this.config = config;
        this.operations = operations;
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public boolean scan(String filnavn, byte[] data) {
        try {
            if (!ServiceUtils.isRunningInProd() && filnavn.startsWith("virustest")) {
                return false;
            }
            LOG.info("Scanner {} bytes", data.length);
            ScanResult[] scanResults = putForObject(config.getUri(), data);
            if (scanResults.length != 1) {
                LOG.warn("Uventet respons med lengde {}, forventet lengde er 1", scanResults.length);
                return true;
            }
            ScanResult scanResult = scanResults[0];
            LOG.info("Fikk scan result {}", scanResult);
            if (OK.equals(scanResult.getResult())) {
                LOG.info("Ingen virus i {}", filnavn);
                INGENVIRUS_COUNTER.incrementAndGet();
                return true;
            }
            LOG.warn("Fant virus i {}, status {}", filnavn, scanResult.getResult());
            VIRUS_COUNTER.incrementAndGet();
            return false;
        } catch (Exception e) {
            LOG.warn("Kunne ikke scanne {}", filnavn, e);
            return true;
        }
    }

    private ScanResult[] putForObject(URI uri, Object payload) {
        return operations.exchange(RequestEntity.put(uri).body(payload), ScanResult[].class).getBody();
    }
}
