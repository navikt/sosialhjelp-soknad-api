package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import static java.lang.System.getenv;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
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
        final Invocation.Builder request = lagRequest(endpoint + "/enhet/navkontor/" + geografiskTilknytning);

        try {
            response = request.get();
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
    public RsKontaktinformasjon hentKontaktinformasjonForEnhet(String enhetNr) {
        Response response = null;
        final Invocation.Builder request = lagRequest(endpoint + "/enhet/" + enhetNr + "/kontaktinformasjon");

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
        String consumerId = getSubjectHandler().getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP-SERVER-NORG2_API_V1-APIKEY_USERNAME");

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
