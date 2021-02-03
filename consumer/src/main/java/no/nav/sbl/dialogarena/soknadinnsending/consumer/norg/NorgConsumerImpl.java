package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import io.github.resilience4j.retry.Retry;
import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static java.lang.System.getenv;
import static no.nav.sbl.dialogarena.retry.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER;
import static no.nav.sbl.dialogarena.retry.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS;
import static no.nav.sbl.dialogarena.retry.RetryUtils.DEFAULT_MAX_ATTEMPTS;
import static no.nav.sbl.dialogarena.retry.RetryUtils.retryConfig;
import static no.nav.sbl.dialogarena.retry.RetryUtils.withRetry;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_NAV_APIKEY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class NorgConsumerImpl implements NorgConsumer {

    private static final Logger logger = getLogger(NorgConsumerImpl.class);
    private static final String SOKNADSOSIALHJELP_SERVER_NORG_2_API_V_1_APIKEY_PASSWORD = "SOKNADSOSIALHJELP_SERVER_NORG2_API_V1_APIKEY_PASSWORD";

    private final Client client;
    private final String endpoint;
    private final Retry retry;

    public NorgConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
        this.retry = retryConfig(
                endpoint,
                DEFAULT_MAX_ATTEMPTS,
                DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                new Class[]{ServerErrorException.class},
                logger);
    }

    @Override
    public RsNorgEnhet getEnhetForGeografiskTilknytning(String geografiskTilknytning) {

        final Invocation.Builder request = lagRequest(endpoint + "enhet/navkontor/" + geografiskTilknytning);
        try {
            var response = withRetry(retry, request::get);
            if (response.getStatus() != 200) {
                logger.warn("Feil statuskode ved kall mot NORG/gt: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
                return null;
            }
            return response.readEntity(RsNorgEnhet.class);
        } catch (NotFoundException e) {
            logger.warn("Fant ikke norgenhet for gt {}", geografiskTilknytning);
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til NORG/gt", e);
            throw new TjenesteUtilgjengeligException("NORG", e);
        }
    }
    
    @Override
    public void ping() {
        /* 
         * Erstatt denne metoden med et skikkelig ping-kall. Vi bruker nå et
         * urelatert tjenestekall fordi denne gir raskt svar (og verifiserer
         * at vi når tjenesten).
         */
        final String consumerId = SubjectHandler.getConsumerId();
        final String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv(SOKNADSOSIALHJELP_SERVER_NORG_2_API_V_1_APIKEY_PASSWORD);

        final Invocation.Builder request = client.target(endpoint + "kodeverk/EnhetstyperNorg").request()
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId)
                .header(HEADER_NAV_APIKEY, apiKey);

        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                throw new RuntimeException("Feil statuskode ved kall mot NORG/gt: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }

    private Invocation.Builder lagRequest(String endpoint) {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv(SOKNADSOSIALHJELP_SERVER_NORG_2_API_V_1_APIKEY_PASSWORD);

        WebTarget b = client.target(endpoint);

        if (isNotEmpty(apiKey)) {
            return b.request()
                    .header(HEADER_CALL_ID, callId)
                    .header(HEADER_CONSUMER_ID, consumerId)
                    .header(HEADER_NAV_APIKEY, apiKey);
        }
        return b.request()
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId);
    }

}
