package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.FssToken;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.sts.STSConsumer;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ArbeidsforholdConsumerImpl implements ArbeidsforholdConsumer {

    private static final Logger logger = getLogger(ArbeidsforholdConsumerImpl.class);
    private static final String A_ORDNINGEN = "A_ORDNINGEN";
    private static final String BEARER = "Bearer ";

    private Client client;
    private String endpoint;

    @Inject
    private STSConsumer stsConsumer;

    public ArbeidsforholdConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public void ping() {
        String consumerId = OidcFeatureToggleUtils.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);

        Invocation.Builder request = client.target(endpoint + "v1/").request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);

        try (Response response = request.options()) {
            if (response.getStatus() != 200) {
                throw new RuntimeException("Feil statuskode ved ping mot aareg.api: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
            }
        }
    }

    @Override
    public List<ArbeidsforholdDto> finnArbeidsforholdForArbeidstaker(String fodselsnummer) {
        Invocation.Builder request = lagRequest(endpoint + "v1/arbeidstaker/arbeidsforhold", fodselsnummer);
        try (Response response = request.get()) {
            if (response.getStatus() != 200) {
                logger.warn("Feil statuskode ved kall mot aareg.api: " + response.getStatus() + ", respons: " + response.readEntity(String.class));
                return null;
            }
            return response.readEntity(new GenericType<List<ArbeidsforholdDto>>() {
            });
        } catch (BadRequestException e) {
            logger.warn("Ugyldig(e) parameter(e) i request");
            return null;
        } catch (NotAuthorizedException e) {
            logger.warn("Token mangler eller er ugyldig");
            return null;
        } catch (ForbiddenException e) {
            logger.warn("Ingen tilgang til forespurt ressurs");
            return null;
        } catch (NotFoundException e) {
            logger.warn("Fant ikke arbeidsforhold for bruker");
            return null;
        } catch (RuntimeException e) {
            logger.warn("Noe uventet feilet ved kall til Arbeidsforhold_v1", e);
            throw new TjenesteUtilgjengeligException("AAREG", e);
        }
    }

    private Invocation.Builder lagRequest(String endpoint, String fodselsnummer) {
        String consumerId = OidcFeatureToggleUtils.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        FssToken fssToken = stsConsumer.getFSSToken();

        return client.target(endpoint)
                .queryParam("sporingsinformasjon", false)
                .queryParam("regeverk", A_ORDNINGEN)
                .request()
                .header("Authorization", OidcFeatureToggleUtils.getToken()) // brukers token?
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("Nav-Consumer-Token", BEARER + fssToken.getAccessToken())
                .header("Nav-Personident", fodselsnummer);
    }
}
