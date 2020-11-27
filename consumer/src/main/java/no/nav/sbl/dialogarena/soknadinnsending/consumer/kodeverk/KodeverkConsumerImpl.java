package no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk;


import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.KodeverkDto;
import org.slf4j.Logger;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.net.URI;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_ID;
import static org.slf4j.LoggerFactory.getLogger;

public class KodeverkConsumerImpl implements KodeverkConsumer {

    private static final Logger logger = getLogger(KodeverkConsumerImpl.class);

    private static final String PATH_PING = "internal/isAlive";
    private static final String QUERY = "ekskluderUgyldige=true&spraak=nb";

    private static final String POSTNUMMER = "Postnummer";
    private static final String KOMMUNER = "Kommuner";
    private static final String LANDKODER = "Landkoder";

    private final Client client;
    private final String endpoint;

    public KodeverkConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
//        Invocation.Builder request = client.target(endpoint + PATH_PING).request();
//        try (Response response = request.get()) {
//            if (response.getStatus() != 200) {
//                throw new RuntimeException("Ping mot kodeverk feilet: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
//            }
//        }

        try {
            hentPostnummer();
        } catch (Exception e) {
            throw new RuntimeException("Ping mot kodeverk feilet");
        }
    }

    @Override
    public KodeverkDto hentPostnummer() {
        return hentKodeverk(POSTNUMMER);
    }

    @Override
    public KodeverkDto hentKommuner() {
        return hentKodeverk(KOMMUNER);
    }

    @Override
    public KodeverkDto hentLandkoder() {
        return hentKodeverk(LANDKODER);
    }

    private KodeverkDto hentKodeverk(String kodeverksnavn) {
        try {
            return lagRequest(kodeverkUri(kodeverksnavn)).get(KodeverkDto.class);
        } catch (ClientErrorException e) {
            throw new TjenesteUtilgjengeligException("Kodeverk - client-feil", e);
        } catch (Exception e) {
            throw new TjenesteUtilgjengeligException("Kodeverk - noe uventet feilet", e);
        }
    }

    private Invocation.Builder lagRequest(URI uri) {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        WebTarget b = client.target(uri);

        return b.request()
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId);
    }

    private URI kodeverkUri(String kodeverksnavn) {
        return URI.create(endpoint + "api/v1/kodeverk/" + kodeverksnavn + "/koder/betydninger" + QUERY);
    }

}
