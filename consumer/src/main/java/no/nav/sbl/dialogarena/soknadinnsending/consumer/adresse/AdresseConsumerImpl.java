package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseConsumer;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

public class AdresseConsumerImpl implements AdresseConsumer {

    private static final Logger logger = getLogger(AdresseConsumerImpl.class);

    private Client client;
    private String endpoint;

    public AdresseConsumerImpl(Client client, String endpoint) {
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
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return null;
    }

    private Invocation.Builder lagRequest(String adresse) {
        return client.target(endpoint + "/adressesoek")
                .queryParam("adresse", adresse)
                .queryParam("soketype", "E")
                .queryParam("alltidRetur", "true")
                .queryParam("maxretur", "100")
                .request()
                .header("Nav-Call-Id", "dummy")
                .header("Nav-Consumer-Id", "srvSosialhjelp");
    }


}
