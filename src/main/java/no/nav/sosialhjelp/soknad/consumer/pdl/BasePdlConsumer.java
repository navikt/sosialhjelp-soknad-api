package no.nav.sosialhjelp.soknad.consumer.pdl;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;
import no.nav.sosialhjelp.soknad.client.sts.StsClient;
import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlBaseResponse;
import no.nav.sosialhjelp.soknad.consumer.pdl.common.PdlRequest;
import no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.slf4j.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.DEFAULT_MAX_ATTEMPTS;
import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.retryConfig;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.BEARER;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_TOKEN;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

public abstract class BasePdlConsumer {

    protected final Client client;
    protected final String endpoint;
    protected final StsClient stsClient;
    protected final Retry retry;

    protected BasePdlConsumer(Client client, String endpoint, StsClient stsClient, Logger log) {
        this.client = client;
        this.endpoint = endpoint;
        this.stsClient = stsClient;
        this.retry = retryConfig(
                endpoint,
                DEFAULT_MAX_ATTEMPTS,
                DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
                DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER,
                new Class[]{WebApplicationException.class, ProcessingException.class},
                log);
    }

    public void ping() {
        var consumerId = SubjectHandler.getConsumerId();
        var callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        var request = client.target(endpoint).request()
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId);

        try (var response = request.options()) {
            if (response.getStatus() != 200) {
                throw new RuntimeException("pdl-api - Feil statuskode ved ping: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }

    protected Entity<PdlRequest> requestEntity(String query, Map<String, Object> variables) {
        var request = new PdlRequest(query, variables);
        return Entity.entity(request, MediaType.APPLICATION_JSON_TYPE);
    }

    protected Invocation.Builder baseRequest(String endpoint) {
        var consumerId = SubjectHandler.getConsumerId();
        var callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        var fssToken = stsClient.getFssToken();

        return client.target(endpoint)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(AUTHORIZATION.name(), BEARER + fssToken.getAccess_token())
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId)
                .header(HEADER_CONSUMER_TOKEN, BEARER + fssToken.getAccess_token());
    }

    protected  <T> T withRetry(CheckedFunction0<T> supplier) {
        return RetryUtils.withRetry(retry, supplier);
    }

    protected void checkForPdlApiErrors(PdlBaseResponse response) {
        Optional.ofNullable(response)
                .map(PdlBaseResponse::getErrors)
                .ifPresent(this::handleErrors);
    }

    private void handleErrors(List<JsonNode> errorJsonNodes) {
        var errors = errorJsonNodes.stream()
                .map(jsonNode -> jsonNode.get("message") + "(feilkode: " + jsonNode.path("extensions").path("code") + ")")
                .collect(Collectors.toList());
        throw new PdlApiException(errorMessage(errors));
    }

    private String errorMessage(List<String> errors) {
        var stringJoiner = new StringJoiner("\n");
        stringJoiner.add("Error i respons fra pdl-api: ");
        errors.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
}
