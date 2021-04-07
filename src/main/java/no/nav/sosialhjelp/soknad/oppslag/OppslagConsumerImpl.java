package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.oppslag.dto.Ident;
import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class OppslagConsumerImpl implements OppslagConsumer {

    private static final Logger logger = getLogger(OppslagConsumerImpl.class);

    private final Client client;
    private final String endpoint;

    public OppslagConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
        var request = client.target(endpoint + "ping").request();
        try (var response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Ping feilet mot oppslag.kontonummer: {}", response.getStatus());
            }
        }
    }

    @Override
    public KontonummerDto getKontonummer(String ident) {
        var request = lagRequest(endpoint + "kontonummer");
        try {
            return request.post(requestEntity(ident), KontonummerDto.class);
        } catch (NotAuthorizedException e) {
            logger.warn("oppslag.kontonummer - 401 Unauthorized - {}", e.getMessage());
            return null;
        } catch (ForbiddenException e) {
            logger.warn("oppslag.kontonummer - 403 Forbidden - {}", e.getMessage());
            return null;
        } catch (NotFoundException e) {
            logger.warn("oppslag.kontonummer - 404 Not Found - {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("oppslag.kontonummer - Noe uventet feilet");
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

    private Entity<Ident> requestEntity(String ident) {
        var request = new Ident(ident);
        return Entity.entity(request, MediaType.APPLICATION_JSON_TYPE);
    }
}
