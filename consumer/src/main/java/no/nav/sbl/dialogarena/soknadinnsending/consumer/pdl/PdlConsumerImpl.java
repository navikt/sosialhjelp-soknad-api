package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;
import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.retry.RetryUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.PdlHentPersonResponse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.PdlRequest;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.FssToken;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.STSConsumer;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.retry.RetryUtils.DEFAULT_MAX_ATTEMPTS;
import static no.nav.sbl.dialogarena.retry.RetryUtils.DEFAULT_RETRY_WAIT_MILLIS;
import static no.nav.sbl.dialogarena.retry.RetryUtils.retryConfig;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.BEARER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_TOKEN;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_TEMA;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.TEMA_KOM;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;
import static org.slf4j.LoggerFactory.getLogger;

public class PdlConsumerImpl implements PdlConsumer {

    private final static Logger log = getLogger(PdlConsumerImpl.class);

    private final Client client;
    private final String endpoint;
    private final STSConsumer stsConsumer;
    private final Retry retry;

    private final ObjectMapper pdlMapper = JsonMapper.builder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .addModule(new JavaTimeModule())
            .build();

    public PdlConsumerImpl(Client client, String endpoint, STSConsumer stsConsumer) {
        this.client = client;
        this.endpoint = endpoint;
        this.stsConsumer = stsConsumer;
        this.retry = retryConfig(endpoint, DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_WAIT_MILLIS, new Class[]{WebApplicationException.class, ProcessingException.class}, log);
    }

    @Override
    @Cacheable(value = "pdlPersonCache", key = "#ident")
    public PdlPerson hentPerson(String ident) {
        String query = PdlApiQuery.HENT_PERSON;
        try {
            var request = lagRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(ident, query), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<PdlHentPersonResponse<PdlPerson>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentPerson)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    @Cacheable(value = "pdlBarnCache", key = "#ident")
    public PdlBarn hentBarn(String ident) {
        String query = PdlApiQuery.HENT_BARN;
        try {
            var request = lagRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(ident, query), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<PdlHentPersonResponse<PdlBarn>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentBarn)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    @Override
    @Cacheable(value = "pdlEktefelleCache", key = "#ident")
    public PdlEktefelle hentEktefelle(String ident) {
        String query = PdlApiQuery.HENT_EKTEFELLE;
        try {
            var request = lagRequest(endpoint);
            var body = withRetry(() -> request.post(requestEntity(ident, query), String.class));
            var pdlResponse = pdlMapper.readValue(body, new TypeReference<PdlHentPersonResponse<PdlEktefelle>>() {});

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (PdlApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Kall til PDL feilet (hentEktefelle)");
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
    }

    private Entity<PdlRequest> requestEntity(String ident, String query) {
        PdlRequest request = new PdlRequest(
                query,
                Map.of(
                        "historikk", false,
                        "ident", ident)
        );
        return Entity.entity(request, MediaType.APPLICATION_JSON_TYPE);
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
                throw new RuntimeException("pdl-api - Feil statuskode ved ping: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }

    private Invocation.Builder lagRequest(String endpoint) {
        String consumerId = SubjectHandler.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        FssToken fssToken = stsConsumer.getFSSToken();

        return client.target(endpoint)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION.name(), BEARER + fssToken.getAccessToken())
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId)
                .header(HEADER_CONSUMER_TOKEN, BEARER + fssToken.getAccessToken())
                .header(HEADER_TEMA, TEMA_KOM);
    }

    private <T> T withRetry(CheckedFunction0<T> supplier) {
        return RetryUtils.withRetry(retry, supplier);
    }

    private void checkForPdlApiErrors(PdlHentPersonResponse response) {
        Optional.ofNullable(response)
                .map(PdlHentPersonResponse::getErrors)
                .ifPresent(this::handleErrors);
    }

    private void handleErrors(List<JsonNode> errorJsonNodes) {
        List<String> errors = errorJsonNodes.stream()
                .map(jsonNode -> jsonNode.get("message") + "(feilkode: " + jsonNode.path("extensions").path("code") + ")")
                .collect(Collectors.toList());
        throw new PdlApiException(errorMessage(errors));
    }

    private String errorMessage(List<String> errors) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("Error i respons fra pdl-api: ");
        errors.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
}
