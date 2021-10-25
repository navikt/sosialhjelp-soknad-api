package no.nav.sosialhjelp.soknad.consumer.skatt;

import no.nav.sosialhjelp.soknad.client.skatteetaten.dto.SkattbarInntekt;
import no.nav.sosialhjelp.soknad.consumer.concurrency.RestCallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.maskerFnr;

@Component
public class SkattbarInntektConsumerImpl implements SkattbarInntektConsumer {

    private static final Logger log = LoggerFactory.getLogger(SkattbarInntektConsumerImpl.class);

    private final Client client;
    private final String endpoint;

    private final Function<Sokedata, RestCallContext> restCallContextSelector;

    public SkattbarInntektConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;

        restCallContextSelector = (sokedata -> new RestCallContext.Builder()
                .withClient(client)
                .withConcurrentRequests(2)
                .withMaximumQueueSize(6)
                .withExecutorTimeoutInMilliseconds(30000)
                .build());
    }

    @Override
    public SkattbarInntekt hentSkattbarInntekt(String fnummer) {
        Sokedata sokedata = new Sokedata()
                .withFom(LocalDate.now().minusMonths(LocalDate.now().getDayOfMonth() > 10 ? 1 : 2))
                .withTom(LocalDate.now()).withIdentifikator(fnummer);

        Invocation.Builder request = getRequest(sokedata);

        try (Response response = request.get()) {
            if (log.isDebugEnabled()) {
                response.bufferEntity();
                log.debug("Response ({}): {}", response.getStatus(), response.readEntity(String.class));
            }

            if (response.getStatus() == 200) {
                return response.readEntity(SkattbarInntekt.class);
            } else if (response.getStatus() == 404) {
                // Ingen funnet
                return new SkattbarInntekt();
            } else {
                String melding = response.readEntity(String.class);
                var feilmeldingUtenFnr = maskerFnr(melding);
                log.warn("Klarer ikke hente skatteopplysninger {} status {}} ", feilmeldingUtenFnr, response.getStatus());
                return null;
            }
        } catch (RuntimeException e) {
            log.warn("Klarer ikke hente skatteopplysninger", e);
            return null;
        }
    }

    @Override
    public void ping() {
        Invocation.Builder request = client.target(endpoint).request();
        try (Response response = request.options()) {
            if (response.getStatus() != 200) {
                log.warn("Ping feilet mot Skatteetaten. {} - {}", response.getStatus(), response.getStatusInfo().getReasonPhrase());
            }
        }
    }

    public Invocation.Builder getRequest(Sokedata sokedata) {
        RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        return lagRequest(executionContext, sokedata);
    }

    private Invocation.Builder lagRequest(RestCallContext executionContext, Sokedata sokedata) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        WebTarget b = executionContext.getClient().target(String.format("%s%s/oppgave/inntekt", endpoint, sokedata.identifikator))
                .queryParam("fraOgMed", sokedata.fom.format(formatter))
                .queryParam("tilOgMed", sokedata.tom.format(formatter));
        return b.request();
    }

    public static class Sokedata {
        //Builder med personidentifikator og fom tom, brukes som parametere til rest kallet
        public String identifikator;
        public LocalDate fom;
        public LocalDate tom;

        public Sokedata withIdentifikator(String identifikator) {
            this.identifikator = identifikator;
            return this;
        }

        public Sokedata withFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Sokedata withTom(LocalDate tom) {
            this.tom = tom;
            return this;
        }

        @Override
        public String toString() {
            return "Sokedata{" +
                    "identifikator='" + identifikator + '\'' +
                    ", fom=" + fom +
                    ", tom=" + tom +
                    '}';
        }
    }
}
