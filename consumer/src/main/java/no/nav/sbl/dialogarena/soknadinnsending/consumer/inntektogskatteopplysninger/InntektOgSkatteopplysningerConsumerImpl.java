package no.nav.sbl.dialogarena.soknadinnsending.consumer.inntektogskatteopplysninger;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.function.Function;

import static java.lang.System.getenv;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;
// Trenger virksomhetssertifikat fra
// GET https://<env>/api/innrapportert/inntektsmottaker/<rettighetspakke>/<personidentifikator>/oppgave/inntekt?fraOgMed=<YYYY-MM>[&tilOgMed=<YYYY-MM>]
// curl -k -v --cert datakonsument.cer --key datakonsument.key "https://api-at.sits.no/api/innrapportert/inntektsmottaker/12345678901/oppgave/inntekt?fraOgMed=2019-01&tilOgMed=2019-02" > resultat.json


// Hent fra ekstern tjeneste, og fyll inn i reponsobjekt, vi maa identifisere hvor disse dataene ligger

/*
* Følgende inntekts- og skatteopplysninger er nødvendige:

   Lønnsinntekt fra arbeidsgivere
   Utbetalt pensjon fra andre (private) pensjonsselskaper
   Ventelønn fra andre offentlige etater
   Lott for fiskere
   Vederlag for lønnsarbeid i hjemmet (dagmamma mv.)
* */
public class InntektOgSkatteopplysningerConsumerImpl implements InntektOgskatteopplysningerConsumer {

    private static Logger logger = getLogger(InntektOgSkatteopplysningerConsumerImpl.class);

    private Function<Sokedata, RestCallContext> restCallContextSelector;
    private String endpoint;


    public InntektOgSkatteopplysningerConsumerImpl(RestCallContext restCallContext, String endpoint) {
        this((sokedata) -> restCallContext, endpoint);
    }

    public InntektOgSkatteopplysningerConsumerImpl(Function<Sokedata, RestCallContext> restCallContextSelector, String endpoint) {
        this.restCallContextSelector = restCallContextSelector;
        this.endpoint = "https://api-at.sits.no";//endpoint;
    }


    @Override
    public InntektOgskatteopplysningerRespons sok(Sokedata sokedata) {
        RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        Builder request = lagRequest(executionContext, sokedata);

        return RestCallUtils.performRequestUsingContext(executionContext, () -> {
            return hentOpplysninger(sokedata, request);
        });
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
            throw new TjenesteUtilgjengeligException("TPS adresse", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private InntektOgskatteopplysningerRespons lagRespons(Sokedata sokedata, Response response) {
        InntektOgskatteopplysningerRespons result = response.readEntity(InntektOgskatteopplysningerRespons.class);
        //   taMedDataFraRequest(sokedata, result);
        return result;
    }

    private Builder lagRequest(RestCallContext executionContext, Sokedata sokedata) {
        String consumerId = getSubjectHandler().getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD");

        WebTarget b = executionContext.getClient().target(String.format("%s/api/innrapportert/inntektsmottaker/%s/%s/oppgave/inntekt", endpoint, "rettighetspakke", sokedata.identifikator))
                .queryParam("fraOgMed", sokedata.fom)
                .queryParam("tilOgMed", sokedata.tom);

        return b.request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);
    }
}
