package no.nav.sosialhjelp.soknad.oppslag;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.Retry;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingerResponseDto;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.CACHE_30_MINUTES_IN_SECONDS;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.NAVUTBETALINGER_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.objectMapper;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.retryConfig;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.withRetry;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class OppslagConsumerImpl implements OppslagConsumer {

    private static final Logger log = getLogger(OppslagConsumerImpl.class);

    private final Client client;
    private final String endpoint;
    private final Retry retry;
    private final RedisService redisService;

    public OppslagConsumerImpl(
            Client client,
            String endpoint,
            RedisService redisService
    ) {
        this.client = client;
        this.endpoint = endpoint;
        this.retry = retryConfig(
                endpoint,
                new Class[]{ServerErrorException.class},
                log);
        this.redisService = redisService;
    }

    @Override
    public void ping() {
        var request = client.target(endpoint + "ping").request();
        try (var response = request.get()) {
            if (response.getStatus() != 200) {
                log.warn("Ping feilet mot oppslag.kontonummer: {}", response.getStatus());
            }
        }
    }

    @Override
    public UtbetalingerResponseDto getUtbetalingerSiste40Dager(String ident) {
        return Optional.ofNullable(hentNavUtbetalingerFraCache(ident))
                .orElse(hentNavUtbetalingerFraOppslagApi(ident));
    }

    private UtbetalingerResponseDto hentNavUtbetalingerFraCache(String ident) {
        return (UtbetalingerResponseDto) redisService.get(NAVUTBETALINGER_CACHE_KEY_PREFIX + ident, UtbetalingerResponseDto.class);
    }

    private UtbetalingerResponseDto hentNavUtbetalingerFraOppslagApi(String ident) {
        var request = lagRequest(endpoint + "utbetalinger");
        try {
            var utbetalingerResponseDto = withRetry(retry, () -> request.get(new GenericType<UtbetalingerResponseDto>() {}));
            lagreNavUtbetalingerTilCache(ident, utbetalingerResponseDto);
            return utbetalingerResponseDto;
        } catch (Exception e) {
            log.error("oppslag.utbetalinger - Noe uventet feilet", e);
            return null;
        }
    }

    private void lagreNavUtbetalingerTilCache(String ident, UtbetalingerResponseDto utbetalingerResponseDto) {
        try {
            redisService.setex(NAVUTBETALINGER_CACHE_KEY_PREFIX + ident, objectMapper.writeValueAsBytes(utbetalingerResponseDto), CACHE_30_MINUTES_IN_SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("Noe feilet ved lagring av utbetalingerResponseDto til redis", e);
        }
    }

    private Invocation.Builder lagRequest(String endpoint) {
        var consumerId = SubjectHandler.getConsumerId();
        var callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        return client.target(endpoint)
                .request()
                .header(HttpHeader.AUTHORIZATION.name(), BEARER + SubjectHandler.getToken())
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId);
    }

}
