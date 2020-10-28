package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.PdlRequest;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.PdlResponse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.FssToken;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.STSConsumer;

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

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.BEARER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_ID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CONSUMER_TOKEN;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_TEMA;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.TEMA_KOM;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

public class PdlConsumerImpl implements PdlConsumer {

    private final Client client;
    private final String endpoint;
    private final STSConsumer stsConsumer;

    public PdlConsumerImpl(Client client, String endpoint, STSConsumer stsConsumer) {
        this.client = client;
        this.endpoint = endpoint;
        this.stsConsumer = stsConsumer;
    }

    @Override
    public PdlPerson hentPerson(String ident) {
        Invocation.Builder builder = lagRequest(endpoint);
        PdlRequest request = new PdlRequest(
                PdlApiQuery.HENT_PERSON,
                Map.of(
                        "historikk", false,
                        "ident", ident)
        );

        try {
            PdlResponse pdlResponse = builder.post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), PdlResponse.class);

            checkForPdlApiErrors(pdlResponse);

            return pdlResponse.getData().getHentPerson();
        } catch (Exception e) {
            throw new TjenesteUtilgjengeligException("Noe uventet feilet ved kall til PDL", e);
        }
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
                .header(AUTHORIZATION.name(), BEARER + fssToken.getAccessToken()) // todo: eller brukers token??
                .header(HEADER_CALL_ID, callId)
                .header(HEADER_CONSUMER_ID, consumerId)
                .header(HEADER_CONSUMER_TOKEN, BEARER + fssToken.getAccessToken())
                .header(HEADER_TEMA, TEMA_KOM);
    }

    private void checkForPdlApiErrors(PdlResponse response) {
        Optional.ofNullable(response)
                .map(PdlResponse::getErrors)
                .ifPresent(errorJsonNodes -> {
                            List<String> errors = errorJsonNodes.stream()
                                    .map(jsonNode -> jsonNode.get("message") + "(feilkode: " + jsonNode.path("extensions").path("code") + ")")
                                    .collect(Collectors.toList());
                            throw new PdlApiException(errorMessage(errors));
                        }
                );
    }

    private String errorMessage(List<String> errors) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("Error i respons fra pdl-api: ");
        errors.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }
}
