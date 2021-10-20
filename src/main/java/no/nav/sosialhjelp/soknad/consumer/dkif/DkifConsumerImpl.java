package no.nav.sosialhjelp.soknad.consumer.dkif;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.sosialhjelp.soknad.consumer.dkif.dto.DigitalKontaktinfoBolk;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.CACHE_30_MINUTES_IN_SECONDS;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.DKIF_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.RedisUtils.objectMapper;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_PERSONIDENTER;
import static org.slf4j.LoggerFactory.getLogger;

public class DkifConsumerImpl implements DkifConsumer {

    private static final Logger logger = getLogger(DkifConsumerImpl.class);
    private static final String BEARER = "Bearer ";

    private final Client client;
    private final String endpoint;
    private final RedisService redisService;

    public DkifConsumerImpl(
            Client client,
            String endpoint,
            RedisService redisService
    ) {
        this.client = client;
        this.endpoint = endpoint;
        this.redisService = redisService;
    }

    @Override
    public void ping() {
        Invocation.Builder request = client.target(endpoint + "ping").request();
        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Ping feilet mot Dkif: {}", response.getStatus());
            }
        }
    }

    @Override
    public DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident) {
        return Optional.ofNullable(hentFraCache(ident))
                .orElse(hentFraDkif(ident));
    }

    private DigitalKontaktinfoBolk hentFraCache(String ident) {
        return (DigitalKontaktinfoBolk) redisService.get(DKIF_CACHE_KEY_PREFIX + ident, DigitalKontaktinfoBolk.class);
    }

    private DigitalKontaktinfoBolk hentFraDkif(String ident) {
        Invocation.Builder request = lagRequest(endpoint + "v1/personer/kontaktinformasjon", ident);
        try {
            var digitalKontaktinfoBolk = request.get(DigitalKontaktinfoBolk.class);
            lagreTilCache(ident, digitalKontaktinfoBolk);
            return digitalKontaktinfoBolk;
        } catch (NotAuthorizedException e) {
            logger.warn("Dkif.api - 401 Unauthorized - {}", e.getMessage());
            return null;
        } catch (ForbiddenException e) {
            logger.warn("Dkif.api - 403 Forbidden - {}", e.getMessage());
            return null;
        } catch (NotFoundException e) {
            logger.warn("Dkif.api - 404 Not Found - {}", e.getMessage());
            return null;
        } catch (RuntimeException e) {
            logger.error("Dkif.api - Noe uventet feilet", e);
            throw new TjenesteUtilgjengeligException("Dkif", e);
        }
    }

    private void lagreTilCache(String ident, DigitalKontaktinfoBolk digitalKontaktinfoBolk) {
        try {
            redisService.setex(DKIF_CACHE_KEY_PREFIX + ident, objectMapper.writeValueAsBytes(digitalKontaktinfoBolk), CACHE_30_MINUTES_IN_SECONDS);
        } catch (JsonProcessingException e) {
            logger.warn("Noe feilet ved lagring av digitalKontaktinfoBolk til redis", e);
        }
    }

    private Invocation.Builder lagRequest(String endpoint, String ident) {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        WebTarget b = client.target(endpoint);

        return b.request()
                .header(HttpHeader.AUTHORIZATION.name(), BEARER + SubjectHandler.getToken())
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId)
                .header(HEADER_NAV_PERSONIDENTER, ident);
    }
}
