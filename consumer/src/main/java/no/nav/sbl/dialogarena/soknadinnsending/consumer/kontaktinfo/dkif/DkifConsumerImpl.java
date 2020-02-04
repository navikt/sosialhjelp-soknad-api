package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfoBolk;
import org.slf4j.Logger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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
        Invocation.Builder request = client.target(endpoint + "api/ping").request();
        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Ping feilet mot Dkif: " + response.getStatus());
            }
        }
    }

    @Override
    public DigitalKontaktinfoBolk hentDigitalKontaktinfo(String ident) {
        Invocation.Builder request = lagRequest(endpoint + "api/v1/personer/kontaktinformasjon", ident);
        try {
            DigitalKontaktinfoBolk digitalKontaktinfoBolk = request.get(DigitalKontaktinfoBolk.class);
            return digitalKontaktinfoBolk;
        } catch (NotAuthorizedException e) {
            logger.warn("Dkif-api - 401 Unauthorized - {}", e.getMessage());
            return null;
        } catch (ForbiddenException e) {
            logger.warn("Dkif-api - 403 Forbidden - {}", e.getMessage());
            return null;
        } catch (NotFoundException e) {
            logger.warn("Dkif.api - 404 Not Found - {}", e.getMessage());
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til Dkif API", e);
            throw new TjenesteUtilgjengeligException("Dkif", e);
        }
    }

    private Invocation.Builder lagRequest(String endpoint, String ident) {
        String consumerId = OidcFeatureToggleUtils.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        WebTarget b = client.target(endpoint);

        return b.request()
                .header("Authorization", BEARER + OidcFeatureToggleUtils.getToken()) // brukers token (?)
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("Nav-Personidenter", ident);
    }
}
