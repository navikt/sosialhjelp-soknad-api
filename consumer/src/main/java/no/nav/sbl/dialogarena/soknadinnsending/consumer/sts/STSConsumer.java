package no.nav.sbl.dialogarena.soknadinnsending.consumer.sts;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static org.slf4j.LoggerFactory.getLogger;

public class STSConsumer {

    private static final Logger logger = getLogger(STSConsumer.class);

    private Client client;
    private String endpoint;

    public STSConsumer(Client client, String endpoint) {
        if (isTillatMockRessurs()) {
            return;
        }
        this.client = client;
        this.endpoint = endpoint;
    }

    public void ping() {
        if (isTillatMockRessurs()) {
        } else {
            Invocation.Builder request = client
                    .target(endpoint)
                    .request();
            try (Response response = request.options()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException("Feil statuskode ved ping mot STS: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
                }
            }
        }
    }

    public FssToken getFSSToken() {
        Invocation.Builder request = lagRequest();

        try {
            return request.get(FssToken.class);
        } catch (BadRequestException e) {
            logger.warn("STS - 400 bad request", e);
            throw new ApplicationException("STS - 400 bad request. Endpoint=" + endpoint, e);
        } catch (NotAuthorizedException e) {
            logger.warn("STS - 401 unauthorized", e);
            throw new ApplicationException("STS - 401 Unauthorized. Endpoint=" + endpoint, e);
        } catch (ForbiddenException e) {
            logger.warn("STS - 401 unauthorized", e);
            throw new ApplicationException("STS - 403 Forbidden. Endpoint=" + endpoint, e);
        } catch (NotFoundException e) {
            logger.warn("STS - 401 unauthorized", e);
            throw new ApplicationException("STS - 404 Not Found. Endpoint=" + endpoint, e);
        } catch (ServerErrorException e) {
            logger.warn("STS - {} {} - Tjenesten er ikke tilgjengelig", e.getResponse().getStatus(), e.getResponse().getStatusInfo().getReasonPhrase(), e);
            throw new TjenesteUtilgjengeligException("STS", e);
        } catch (Exception e) {
            logger.warn("Noe feil skjedde ved henting av token fra STS i FSS.");
            throw new ApplicationException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=" + endpoint, e);
        }
    }

    private Invocation.Builder lagRequest() {
        return client.target(endpoint)
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .request();
    }
}
