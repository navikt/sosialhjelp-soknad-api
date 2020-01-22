package no.nav.sbl.dialogarena.soknadinnsending.consumer.sts;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import org.slf4j.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
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
        if (MockUtils.isTillatMockRessurs()) {
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

        try (Response response = request.get()) {
            return readFssToken(response);
        } catch (NotAuthorizedException e) {
            logger.warn("STS gir 401 unauthorized", e);
            throw new ApplicationException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=" + endpoint, e);
        } catch (BadRequestException e) {
            logger.warn("STS gir 400 bad request", e);
            throw new ApplicationException("Noe feil skjedde ved henting av token fra STS i FSS. Endpoint=" + endpoint, e);
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

    private FssToken readFssToken(Response response) {
        try {
            return response.readEntity(FssToken.class);
        } catch (ProcessingException e) {
            throw new RuntimeException("Prosesseringsfeil på FSSToken-respons.", e);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Ulovlig tilstand på FSSToken-respons.", e);
        } catch (Exception e) {
            throw new RuntimeException("Uventet feil på FSSToken-respons.", e);
        }
    }
}
