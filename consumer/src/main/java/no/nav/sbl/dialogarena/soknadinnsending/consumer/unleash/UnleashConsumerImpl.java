package no.nav.sbl.dialogarena.soknadinnsending.consumer.unleash;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_ID;

public class UnleashConsumerImpl implements UnleashConsumer {

    private final Client client;
    private final String endpoint;

    public UnleashConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        Invocation.Builder request = client.target(endpoint).request()
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId);

        try (Response response = request.options()) {
            if (response.getStatus() != 200) {
                throw new RuntimeException("unleash-api - Feil statuskode ved ping: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }
}
