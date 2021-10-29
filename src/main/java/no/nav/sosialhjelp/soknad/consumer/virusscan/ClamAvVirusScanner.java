package no.nav.sosialhjelp.soknad.consumer.virusscan;

import io.github.resilience4j.retry.Retry;
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_MAX_ATTEMPTS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.retryConfig;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.withRetry;
import static no.nav.sosialhjelp.soknad.consumer.virusscan.Result.OK;

@Component
public class ClamAvVirusScanner implements VirusScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClamAvVirusScanner.class);

    private final WebClient webClient;
    private final Retry retry;

    @Value("${soknad.vedlegg.virusscan.enabled}")
    private boolean enabled;

    public ClamAvVirusScanner(URI uri, WebClient webClient) {
        this.webClient = webClient;
        this.retry = retryConfig(
                uri.toString(),
                DEFAULT_MAX_ATTEMPTS,
                DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                new Class[]{HttpServerErrorException.class},
                logger);
    }

    @Override
    public void scan(String filnavn, byte[] data, String behandlingsId, String fileType) {
        if (enabled && isInfected(filnavn, data, behandlingsId, fileType)) {
            throw new OpplastingException(String.format("Fant virus i fil for behandlingsId %s", behandlingsId), null, "vedlegg.opplasting.feil.muligVirus");
        } else if (!enabled) {
            logger.info("Virusscanning er ikke aktivert");
        }
    }

    private boolean isInfected(String filnavn, byte[] data, String behandlingsId, String fileType) {
        try {
            if (ServiceUtils.isNonProduction() && filnavn.startsWith("virustest")) {
                return true;
            }
            logger.info("Scanner {} bytes for fileType {} (fra Tika)", data.length, fileType);

            ScanResult[] scanResults = withRetry(retry, () -> putForObject(data));

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

    private ScanResult[] putForObject(Object payload) {
        return webClient.put()
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(ScanResult[].class)
                .block();
    }
}
