package no.nav.sosialhjelp.soknad.consumer.sts;

import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import org.slf4j.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

public class STSConsumer {

    private static final Logger logger = getLogger(STSConsumer.class);

    private Client client;
    private String endpoint;

    private FssToken cachedFssToken;

    public STSConsumer(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    public void ping() {
        Invocation.Builder request = client
                .target(endpoint)
                .request();
        try (Response response = request.options()) {
            if (response.getStatus() != 200) {
                throw new RuntimeException("Feil statuskode ved ping mot STS: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }

    public FssToken getFSSToken() {
        if (shouldRenew(cachedFssToken)) {
            logger.info("Henter nytt STSToken");
            Invocation.Builder request = lagRequest();
            try {
                FssToken fssToken = request.get(FssToken.class);
                cachedFssToken = fssToken;
                return fssToken;
            } catch (BadRequestException e) {
                logger.warn("STS - 400 Bad Request", e);
                throw new SosialhjelpSoknadApiException("STS - 400 bad request. Endpoint=" + endpoint, e);
            } catch (NotAuthorizedException e) {
                logger.warn("STS - 401 unauthorized", e);
                throw new SosialhjelpSoknadApiException("STS - 401 Unauthorized. Endpoint=" + endpoint, e);
            } catch (ForbiddenException e) {
                logger.warn("STS - 403 Forbidden", e);
                throw new SosialhjelpSoknadApiException("STS - 403 Forbidden. Endpoint=" + endpoint, e);
            } catch (NotFoundException e) {
                logger.warn("STS - 404 Not Found", e);
                throw new SosialhjelpSoknadApiException("STS - 404 Not Found. Endpoint=" + endpoint, e);
            } catch (ServerErrorException e) {
                logger.error("STS - {} {} - Tjenesten er ikke tilgjengelig", e.getResponse().getStatus(), e.getResponse().getStatusInfo().getReasonPhrase(), e);
                throw new TjenesteUtilgjengeligException("STS", e);
            } catch (Exception e) {
                logger.error("Noe feil skjedde ved henting av token fra STS i FSS.");
                throw new TjenesteUtilgjengeligException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=" + endpoint, e);
            }
        }
        logger.debug("Tar i bruk cached STSToken");
        return cachedFssToken;
    }

    private boolean shouldRenew(FssToken fssToken) {
        if (fssToken == null) {
            return true;
        }
        return fssToken.isExpired();
    }

    private Invocation.Builder lagRequest() {
        return client.target(endpoint)
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .request();
    }
}
