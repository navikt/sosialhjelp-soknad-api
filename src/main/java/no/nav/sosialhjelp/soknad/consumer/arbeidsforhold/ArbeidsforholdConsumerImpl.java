//package no.nav.sosialhjelp.soknad.consumer.arbeidsforhold;
//
//import no.nav.sosialhjelp.soknad.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
//import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
//import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
//import no.nav.sosialhjelp.soknad.consumer.sts.FssToken;
//import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import org.slf4j.Logger;
//
//import javax.ws.rs.BadRequestException;
//import javax.ws.rs.ForbiddenException;
//import javax.ws.rs.InternalServerErrorException;
//import javax.ws.rs.NotAuthorizedException;
//import javax.ws.rs.NotFoundException;
//import javax.ws.rs.ServiceUnavailableException;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.Invocation;
//import javax.ws.rs.core.GenericType;
//import javax.ws.rs.core.Response;
//import java.time.LocalDate;
//import java.util.List;
//
//import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_TOKEN;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_PERSONIDENT;
//import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class ArbeidsforholdConsumerImpl implements ArbeidsforholdConsumer {
//
//    private static final Logger log = getLogger(ArbeidsforholdConsumerImpl.class);
//    private static final String A_ORDNINGEN = "A_ORDNINGEN";
//    private static final String BEARER = "Bearer ";
//
//    private final Client client;
//    private final String endpoint;
//    private final STSConsumer stsConsumer;
//
//    public ArbeidsforholdConsumerImpl(Client client, String endpoint, STSConsumer stsConsumer) {
//        this.client = client;
//        this.endpoint = endpoint;
//        this.stsConsumer = stsConsumer;
//    }
//
//    @Override
//    public void ping() {
//        String consumerId = SubjectHandler.getConsumerId();
//        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
//
//        Invocation.Builder request = client.target(endpoint + "ping").request()
//                .header(HEADER_CALL_ID, callId)
//                .header(HEADER_CONSUMER_ID, consumerId);
//
//        try (Response response = request.options()) {
//            if (response.getStatus() != 200) {
//                throw new RuntimeException("Aareg.api - Feil statuskode ved ping: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
//            }
//        }
//    }
//
//    @Override
//    public List<ArbeidsforholdDto> finnArbeidsforholdForArbeidstaker(String fodselsnummer) {
//        Invocation.Builder request = lagRequest(endpoint + "v1/arbeidstaker/arbeidsforhold", fodselsnummer);
//        try {
//            return request.get(new GenericType<List<ArbeidsforholdDto>>() {});
//        } catch (BadRequestException e) {
//            log.warn("Aareg.api - 400 Bad Request - Ugyldig(e) parameter(e) i request", e);
//            return null;
//        } catch (NotAuthorizedException e) {
//            log.warn("Aareg.api - 401 Unauthorized- Token mangler eller er ugyldig", e);
//            return null;
//        } catch (ForbiddenException e) {
//            log.warn("Aareg.api - 403 Forbidden - Ingen tilgang til forespurt ressurs", e);
//            return null;
//        } catch (NotFoundException e) {
//            log.warn("Aareg.api - 404 Not Found- Fant ikke arbeidsforhold for bruker", e);
//            return null;
//        } catch (ServiceUnavailableException | InternalServerErrorException e) {
//            log.error("Aareg.api - {} {} - Tjenesten er ikke tilgjengelig", e.getResponse().getStatus(), e.getResponse().getStatusInfo().getReasonPhrase(), e);
//            throw new TjenesteUtilgjengeligException("AAREG", e);
//        } catch (Exception e) {
//            log.error("Aareg.api - Noe uventet feilet", e);
//            throw new TjenesteUtilgjengeligException("AAREG", e);
//        }
//    }
//
//    private Invocation.Builder lagRequest(String endpoint, String fodselsnummer) {
//        String consumerId = SubjectHandler.getConsumerId();
//        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
//        FssToken fssToken = stsConsumer.getFSSToken();
//        Sokeperiode sokeperiode = lagSokePeriode();
//
//        return client.target(endpoint)
//                .queryParam("sporingsinformasjon", false)
//                .queryParam("regelverk", A_ORDNINGEN)
//                .queryParam("ansettelsesperiodeFom", sokeperiode.fom.format(ISO_LOCAL_DATE))
//                .queryParam("ansettelsesperiodeTom", sokeperiode.tom.format(ISO_LOCAL_DATE))
//                .request()
//                .header(AUTHORIZATION.name(), BEARER + SubjectHandler.getToken()) // brukers token
//                .header(HEADER_CALL_ID, callId)
//                .header(HEADER_CONSUMER_ID, consumerId)
//                .header(HEADER_CONSUMER_TOKEN, BEARER + fssToken.getAccessToken())
//                .header(HEADER_NAV_PERSONIDENT, fodselsnummer);
//    }
//
//    private Sokeperiode lagSokePeriode() {
//        return new Sokeperiode(LocalDate.now().minusMonths(3), LocalDate.now());
//    }
//
//    private static final class Sokeperiode {
//
//        private final LocalDate fom;
//        private final LocalDate tom;
//
//        public Sokeperiode(LocalDate fom, LocalDate tom) {
//            this.fom = fom;
//            this.tom = tom;
//        }
//
//        public LocalDate getFom() {
//            return fom;
//        }
//
//        public LocalDate getTom() {
//            return tom;
//        }
//
//    }
//}
