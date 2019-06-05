package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class NorgConsumerImpl implements NorgConsumer {

    private static final Logger logger = getLogger(NorgConsumerImpl.class);

    private Client client;
    private String endpoint;

    public NorgConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public RsNorgEnhet finnEnhetForGeografiskTilknytning(String geografiskTilknytning) {
        Response response = null;
        final Invocation.Builder request = lagRequest(endpoint + "enhet/navkontor/" + geografiskTilknytning);

        try {
            response = request.get();
            if (response.getStatus() != 200) {
                logger.warn("Feil statuskode ved kall mot NORG/gt: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
                return null;
            }
            return response.readEntity(RsNorgEnhet.class);
        } catch (NotFoundException e) {
            logger.warn("Fant ikke norgenhet for gt {}", geografiskTilknytning);
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til NORG/gt", e);
            throw new TjenesteUtilgjengeligException("NORG", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    @Override
    public void ping() {
        /* 
         * Erstatt denne metoden med et skikkelig ping-kall. Vi bruker nå et
         * urelatert tjenestekall fordi denne gir raskt svar (og verifiserer
         * at vi når tjenesten).
         */
        final String consumerId = OidcFeatureToggleUtils.getConsumerId();
        final String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_NORG2_API_V1_APIKEY_PASSWORD");

        final Invocation.Builder request = client.target(endpoint + "kodeverk/EnhetstyperNorg").request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("x-nav-apiKey", apiKey);

        Response response = null;
        try {
            response = request.get();
            if (response.getStatus() != 200) {
                throw new RuntimeException("Feil statuskode ved kall mot NORG/gt: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public RsKontaktinformasjon hentKontaktinformasjonForEnhet(String enhetNr) {
        Response response = null;
        final Invocation.Builder request = lagRequest(endpoint + "enhet/" + enhetNr + "/kontaktinformasjon");

        try {
            response = request.get();
            return response.readEntity(RsKontaktinformasjon.class);
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til NORG/kontaktinformasjon", e);
            throw new TjenesteUtilgjengeligException("NORG", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private Invocation.Builder lagRequest(String endpoint) {
        String consumerId = OidcFeatureToggleUtils.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_NORG2_API_V1_APIKEY_PASSWORD");

        WebTarget b = client.target(endpoint);

        if (isNotEmpty(apiKey)) {
            return b.request()
                    .header("Nav-Call-Id", callId)
                    .header("Nav-Consumer-Id", consumerId)
                    .header("x-nav-apiKey", apiKey);
        }
        return b.request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);
    }

}
