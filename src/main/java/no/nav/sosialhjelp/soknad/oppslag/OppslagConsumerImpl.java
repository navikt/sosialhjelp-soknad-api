package no.nav.sosialhjelp.soknad.oppslag;

import io.github.resilience4j.retry.Retry;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;

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

    public OppslagConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
        this.retry = retryConfig(
                endpoint,
                new Class[]{ServerErrorException.class},
                log);
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
    public KontonummerDto getKontonummer(String ident) {
        var request = lagRequest(endpoint + "kontonummer");
        try {
            return withRetry(retry, () -> request.get(KontonummerDto.class));
        } catch (NotAuthorizedException e) {
            log.warn("oppslag.kontonummer - 401 Unauthorized - {}", e.getMessage());
            return null;
        } catch (NotFoundException e) {
            log.warn("oppslag.kontonummer - 404 Not Found - {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("oppslag.kontonummer - Noe uventet feilet");
            throw new TjenesteUtilgjengeligException("oppslag.kontonummer", e);
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
