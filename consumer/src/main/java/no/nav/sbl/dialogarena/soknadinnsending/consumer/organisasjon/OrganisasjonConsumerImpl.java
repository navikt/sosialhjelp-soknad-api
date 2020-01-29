package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
import org.slf4j.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static java.lang.System.getenv;
import static org.slf4j.LoggerFactory.getLogger;

public class OrganisasjonConsumerImpl implements OrganisasjonConsumer {

    private static final Logger logger = getLogger(OrganisasjonConsumerImpl.class);
    private static final String SOSIALHJELP_SOKNAD_API_EREGAPI_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_EREGAPI_APIKEY_PASSWORD";

    private Client client;
    private String endpoint;

    public OrganisasjonConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
        // faker ping med OPTIONS-kall til samme tjeneste med NAV ITs orgnr
        Invocation.Builder request = client.target(endpoint + "v1/organisasjon/990983666/noekkelinfo").request();
        try (Response response = request.options()) {
            if (response.getStatus() != 200) {
                logger.warn("Ping feilet mot Ereg: " + response.getStatus());
            }
        }
    }

    @Override
    public OrganisasjonNoekkelinfoDto hentOrganisasjonNoekkelinfo(String orgnr) {
        Invocation.Builder request = lagRequest(endpoint + "v1/organisasjon/" + orgnr + "/noekkelinfo");
        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Feil statuskode ved kall mot Ereg: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
                return null;
            }
            return response.readEntity(OrganisasjonNoekkelinfoDto.class);
        } catch (NotFoundException e) {
            logger.warn("Fant ikke forespurt(e) entitet(er)");
            return null;
        } catch (BadRequestException e) {
            logger.warn("Ugyldig(e) parameter(e) i request");
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til Ereg API", e);
            throw new TjenesteUtilgjengeligException("EREG", e);
        }
    }

    private Invocation.Builder lagRequest(String endpoint) {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv(SOSIALHJELP_SOKNAD_API_EREGAPI_APIKEY_PASSWORD);

        WebTarget b = client.target(endpoint);

        return b.request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("x-nav-apiKey", apiKey);
    }
}
