package no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.dkif.dto.DigitalKontaktinfoBolk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_PERSONIDENTER;
import static org.slf4j.LoggerFactory.getLogger;

public class DkifConsumerImpl implements DkifConsumer {

    private static final Logger logger = getLogger(DkifConsumerImpl.class);
    private static final String BEARER = "Bearer ";

    private Client client;
    private String endpoint;

    public DkifConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
        Invocation.Builder request = client.target(endpoint + "ping").request();
        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Ping feilet mot Dkif: " + response.getStatus());
            }
        }
    }

    @Override
    @Cacheable("dkifCache")
    public DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident) {
        Invocation.Builder request = lagRequest(endpoint + "v1/personer/kontaktinformasjon", ident);
        try {
            return request.get(DigitalKontaktinfoBolk.class);
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
