package no.nav.sbl.dialogarena.soknadinnsending.consumer.inntektogskatteopplysninger;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.System.getenv;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Fasit navn på apiKey ressurs soknadsosialhjelp-server-eksternapp.skatt.datasamarbeid.api.inntektsmottaker-apiKey
 * Fasit navn på endepunktet i api-gw eksternapp.skatt.datasamarbeid.api.inntektsmottaker
 * <p>
 * GET https://api-gw-q0.adeo.no/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/<personidentifikator>/oppgave/inntekt?fraOgMed=<YYYY-MM>[&tilOgMed=<YYYY-MM>]
 * <p>
 * <p>
 * Følgende inntekts- og skatteopplysninger er nødvendige:
 * <p>
 * Lønnsinntekt fra arbeidsgivere
 * Utbetalt pensjon fra andre (private) pensjonsselskaper
 * Ventelønn fra andre offentlige etater
 * Lott for fiskere
 * Vederlag for lønnsarbeid i hjemmet (dagmamma mv.)
 */
public class InntektOgSkatteopplysningerConsumerImpl implements InntektOgskatteopplysningerConsumer {

    private static Logger logger = getLogger(InntektOgSkatteopplysningerConsumerImpl.class);

    private Function<Sokedata, RestCallContext> restCallContextSelector;
    private String endpoint;


    public InntektOgSkatteopplysningerConsumerImpl(RestCallContext restCallContext, String endpoint) {
        this((sokedata) -> restCallContext, endpoint);
    }

    public InntektOgSkatteopplysningerConsumerImpl(Function<Sokedata, RestCallContext> restCallContextSelector, String endpoint) {
        this.restCallContextSelector = restCallContextSelector;
        this.endpoint = endpoint;
    }


    @Override
    public Optional<List<Utbetaling>> sok(Sokedata sokedata) {
        RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        Builder request = lagRequest(executionContext, sokedata);

        return Optional.of(mapTilUtbetalinger(RestCallUtils.performRequestUsingContext(executionContext, () -> hentOpplysninger(sokedata, request))));
    }

    @Override
    public void ping() {
        String consumerId = System.getProperty("no.nav.modig.security.systemuser.username");
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD");

        RestCallContext restCallContext = restCallContextSelector.apply(null);

        /*
         * Ping-kallet går raskt og utføres derfor direkte mot tjenesten uten begrensninger:
         */
        Builder request = restCallContext.getClient().target(endpoint + "adressesoek")
                .queryParam("maxretur", "PING")
                .request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("x-nav-apiKey", apiKey);

        Response response = null;
        try {
            response = request.get();
            String melding = response.readEntity(String.class);
            int status = response.getStatus();

            if (status == 400) {

                return;
            }

            throw new RuntimeException(status + ": " + melding);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private InntektOgskatteopplysningerRespons hentOpplysninger(Sokedata sokedata, Builder request) {
        Response response = null;
        try {
            response = request.get();

            if (logger.isDebugEnabled()) {
                response.bufferEntity();
                logger.debug("Response (" + response.getStatus() + "): " + response.readEntity(String.class));
            }

            if (response.getStatus() == 200) {
                return lagRespons(sokedata, response);
            } else if (response.getStatus() == 404) {
                // Ingen funnet
                return new InntektOgskatteopplysningerRespons();
            } else {
                String melding = response.readEntity(String.class);
                throw new RuntimeException(melding);
            }
        } catch (RuntimeException e) {
            logger.info("Noe uventet gikk galt ved oppslag av adresse", e);
            throw new TjenesteUtilgjengeligException("Inntekts- og skatteopplysninger", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
   // https://api-gw-q0.adeo.no/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/01234567890/oppgave/inntekt
    private static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private InntektOgskatteopplysningerRespons lagRespons(Sokedata sokedata, Response response) {
        InntektOgskatteopplysningerRespons result = response.readEntity(InntektOgskatteopplysningerRespons.class);
        //   taMedDataFraRequest(sokedata, result);
        return result;
    }

    List<Utbetaling> mapTilUtbetalinger(InntektOgskatteopplysningerRespons repons) {
        return repons.oppgaveInntektsmottaker
                .stream()
                .flatMap(oppgaveInntektsmottaker -> oppgaveInntektsmottaker
                        .inntekt
                        .stream())
                .filter(inntekt -> inntekt
                        .loennsinntekt != null)
                .map(inntekt -> {
                    Utbetaling utbetaling = new Utbetaling();
                    utbetaling.type = "Lønn";
                    utbetaling.brutto = inntekt.beloep;
                    return utbetaling;
                }).collect(toList());
    }

    private Builder lagRequest(RestCallContext executionContext, Sokedata sokedata) {
        String apiKey = getenv("soknadsosialhjelp-server-eksternapp.skatt.datasamarbeid.api.inntektsmottaker-apiKey"); //https://fasit.adeo.no/resources/7504820????
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        WebTarget b = executionContext.getClient().target(String.format("%s/%s/oppgave/inntekt", endpoint, sokedata.identifikator))
                .queryParam("fraOgMed", sokedata.fom.format(formatter))
                .queryParam("tilOgMed", sokedata.tom.format(formatter));

        return b.request()
                .header("x-nav-apiKey", apiKey);
    }
}
