//package no.nav.sosialhjelp.soknad.consumer.organisasjon;
//
//import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
//import no.nav.sosialhjelp.soknad.consumer.organisasjon.dto.OrganisasjonNoekkelinfoDto;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import org.slf4j.Logger;
//
//import javax.ws.rs.BadRequestException;
//import javax.ws.rs.NotFoundException;
//import javax.ws.rs.ServerErrorException;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Invocation;
//import javax.ws.rs.client.WebTarget;
//import javax.ws.rs.core.Response;
//
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class OrganisasjonConsumerImpl implements OrganisasjonConsumer {
//
//    private static final Logger logger = getLogger(OrganisasjonConsumerImpl.class);
//
//    private Client client;
//    private String endpoint;
//
//    public OrganisasjonConsumerImpl(Client client, String endpoint) {
//        this.client = client;
//        this.endpoint = endpoint;
//    }
//
//    @Override
//    public void ping() {
//        // FIXME: faker ping med OPTIONS-kall til samme tjeneste med NAV ITs orgnr
//        Invocation.Builder request = client.target(endpoint + "v1/organisasjon/990983666/noekkelinfo").request();
//        try (Response response = request.options()) {
//            if (response.getStatus() != 200) {
//                logger.warn("Ping feilet mot Ereg: {}", response.getStatus());
//            }
//        }
//    }
//
//    @Override
//    public OrganisasjonNoekkelinfoDto hentOrganisasjonNoekkelinfo(String orgnr) {
//        Invocation.Builder request = lagRequest(endpoint + "v1/organisasjon/" + orgnr + "/noekkelinfo");
//        try {
//            return request.get(OrganisasjonNoekkelinfoDto.class);
//        } catch (NotFoundException e) {
//            logger.warn("Ereg.api - 404 Not Found - Fant ikke forespurt(e) entitet(er)");
//            return null;
//        } catch (BadRequestException e) {
//            logger.warn("Ereg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request");
//            return null;
//        } catch (ServerErrorException e) {
//            logger.error("Ereg.api - {} {} - Tjenesten er utilgjengelig", e.getResponse().getStatus(), e.getResponse().getStatusInfo().getReasonPhrase(), e);
//            throw new TjenesteUtilgjengeligException("EREG", e);
//        } catch (Exception e) {
//            logger.error("Ereg.api - Noe uventet feilet", e);
//            throw new TjenesteUtilgjengeligException("EREG", e);
//        }
//    }
//
//    private Invocation.Builder lagRequest(String endpoint) {
//        String consumerId = SubjectHandler.getConsumerId();
//        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
//
//        WebTarget b = client.target(endpoint);
//
//        return b.request()
//                .header(HEADER_CALL_ID, callId)
//                .header(HEADER_CONSUMER_ID, consumerId);
//    }
//}
