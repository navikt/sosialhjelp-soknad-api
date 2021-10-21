package no.nav.sosialhjelp.soknad.consumer.norg;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.Retry;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.consumer.norg.dto.RsNorgEnhet;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.slf4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

import static java.lang.System.getenv;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.CACHE_24_HOURS_IN_SECONDS;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.GT_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.GT_LAST_POLL_TIME_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.objectMapper;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_MAX_ATTEMPTS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.retryConfig;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.withRetry;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class NorgConsumerImpl implements NorgConsumer {

    private static final Logger logger = getLogger(NorgConsumerImpl.class);
    private static final String NORG2_API_V1_APIKEY = "NORG2_API_V1_APIKEY";

    private final Client client;
    private final String endpoint;
    private final RedisService redisService;
    private final Retry retry;

    public NorgConsumerImpl(Client client, String endpoint, RedisService redisService) {
        this.client = client;
        this.endpoint = endpoint;
        this.redisService = redisService;
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
                logger.warn("Feil statuskode ved kall mot NORG/gt: {}, respons: {}", response.getStatus(), response.readEntity(String.class));
                return null;
            }
            var rsNorgEnhet = response.readEntity(RsNorgEnhet.class);
            lagreTilCache(geografiskTilknytning, rsNorgEnhet);
            return rsNorgEnhet;
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
        final String apiKey = getenv(NORG2_API_V1_APIKEY);

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
        final String apiKey = getenv(NORG2_API_V1_APIKEY);

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

    private void lagreTilCache(String geografiskTilknytning, RsNorgEnhet rsNorgEnhet) {
        try {
            redisService.setex(GT_CACHE_KEY_PREFIX + geografiskTilknytning, objectMapper.writeValueAsBytes(rsNorgEnhet), CACHE_24_HOURS_IN_SECONDS);
            redisService.set(GT_LAST_POLL_TIME_PREFIX + geografiskTilknytning, LocalDateTime.now().format(ISO_LOCAL_DATE_TIME).getBytes(UTF_8));
        } catch (JsonProcessingException e) {
            logger.warn("Noe galt skjedde ved oppdatering av kodeverk til Redis", e);
        }
    }

}
