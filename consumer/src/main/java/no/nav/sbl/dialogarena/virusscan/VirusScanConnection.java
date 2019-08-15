package no.nav.sbl.dialogarena.virusscan;

import static no.nav.sbl.dialogarena.virusscan.Result.OK;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

@Component
class VirusScanConnection {

    private static final Logger LOG = LoggerFactory.getLogger(VirusScanConnection.class);
    private static final AtomicInteger INGENVIRUS_COUNTER = new AtomicInteger();
    private static final AtomicInteger VIRUS_COUNTER = new AtomicInteger();

    private final VirusScanConfig config;
    private final RestOperations operations;

    @Inject
    public VirusScanConnection(VirusScanConfig config) {
        this.config = config;
        this.operations = new RestTemplate();
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
            ScanResult[] scanResults = putForObject(config.getUri(), data, ScanResult[].class);
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

    private <T> T putForObject(URI uri, Object payload, Class<T> responseType) {
        return operations.exchange(RequestEntity.put(uri).body(payload), responseType).getBody();
    }
}
