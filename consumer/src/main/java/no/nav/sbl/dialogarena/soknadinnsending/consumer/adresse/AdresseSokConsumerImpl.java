package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

public class AdresseSokConsumerImpl implements AdresseSokConsumer {

    private static final Logger logger = getLogger(AdresseSokConsumerImpl.class);

    private Client client;
    private String endpoint;

    public AdresseSokConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public AdressesokRespons sokAdresse(String adresse) {
        Invocation.Builder request = lagRequest(adresse);
        Response response = null;

        try {
            response = request.get();
            if (response.getStatus() == 200) {
                return response.readEntity(AdressesokRespons.class);
            } else if (response.getStatus() == 404) {
                // Ingen funnet
                return new AdressesokRespons();
            } else {
                String melding = response.readEntity(String.class);
                throw new RuntimeException(melding);
            }
        } catch (RuntimeException e) {
            logger.info("Noe uventet gikk galt ved oppslag av adresse", e);
            throw e;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private Invocation.Builder lagRequest(String adresse) {
        String consumerId = getSubjectHandler().getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        return client.target(endpoint + "/adressesoek")
                .queryParam("adresse", adresse)
                .queryParam("soketype", "E")
                .queryParam("alltidRetur", "true")
                .queryParam("maxretur", "100")
                .request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);
    }


}
