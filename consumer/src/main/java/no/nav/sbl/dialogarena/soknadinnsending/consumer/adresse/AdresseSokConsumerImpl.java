package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.sbl.dialogarena.mdc.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.function.Function;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class AdresseSokConsumerImpl implements AdresseSokConsumer {

    private static final Logger logger = getLogger(AdresseSokConsumerImpl.class);
    
    private Function<Sokedata, RestCallContext> restCallContextSelector;
    private String endpoint;

    
    public AdresseSokConsumerImpl(RestCallContext restCallContext, String endpoint) {
        this((sokedata) -> restCallContext, endpoint);
    }
    
    public AdresseSokConsumerImpl(Function<Sokedata, RestCallContext> restCallContextSelector, String endpoint) {
        this.restCallContextSelector = restCallContextSelector;
        this.endpoint = endpoint;
    }

    
    @Override
    public AdressesokRespons sokAdresse(String adresse) {
        final Sokedata sokedata = AdresseStringSplitter.toSokedata(adresse);
        return sokAdresse(sokedata);
    }
    
    @Override
    public AdressesokRespons sokAdresse(Sokedata sokedata) {
        final RestCallContext executionContext = restCallContextSelector.apply(sokedata);
        final Invocation.Builder request = lagRequest(executionContext, sokedata, sokedata.soketype.toTpsKode());

        return RestCallUtils.performRequestUsingContext(executionContext, () -> sokAdresseMotTjeneste(sokedata, request));
    }
    
    @Override
    public void ping() {
        final String consumerId = OidcFeatureToggleUtils.getConsumerId();
        final String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD");
        
        final RestCallContext restCallContext = restCallContextSelector.apply(null);
        
        /*
         * Ping-kallet går raskt og utføres derfor direkte mot tjenesten uten begrensninger:
         */
        final Builder request = restCallContext.getClient().target(endpoint + "adressesoek")
                .queryParam("maxretur", "PING") 
                .request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId)
                .header("x-nav-apiKey", apiKey);

        try (Response response = request.get()) {
            final String melding = response.readEntity(String.class);
            final int status = response.getStatus();

            if (status == 400) {
                /*
                 * Vi forventer feilmelding på maxretur. Dette er en stygg hack som
                 * er laget fordi vi mangler et fungerende ping-kall mot TPSWS-REST.
                 *
                 * Vi kan dessverre heller ikke sjekke om feilresponsen inneholder
                 * "maxretur er ikke numerisk" fordi denne fjernes av APIGW.
                 */
                return;
            }

            throw new RuntimeException(status + ": " + melding);
        }
    }

    private AdressesokRespons sokAdresseMotTjeneste(Sokedata sokedata, final Invocation.Builder request) {
        try (Response response = request.get()) {

            if (logger.isDebugEnabled()) {
                response.bufferEntity();
                logger.debug("Response (" + response.getStatus() + "): " + response.readEntity(String.class));
            }

            if (response.getStatus() == 200) {
                return createAdressesokRespons(sokedata, response);
            } else if (response.getStatus() == 404) {
                // Ingen funnet
                return new AdressesokRespons();
            } else {
                String melding = response.readEntity(String.class);
                throw new RuntimeException(melding);
            }
        } catch (RuntimeException e) {
            logger.info("Noe uventet gikk galt ved oppslag av adresse", e);
            throw new TjenesteUtilgjengeligException("TPS adresse", e);
        }
    }

    private AdressesokRespons createAdressesokRespons(Sokedata sokedata, Response response) {
        final AdressesokRespons result = response.readEntity(AdressesokRespons.class);
        taMedDataFraRequest(sokedata, result);
        return result;
    }

    private void taMedDataFraRequest(Sokedata sokedata, AdressesokRespons result) {
        for (AdresseData adresseData : result.adresseDataList) {
            if (skalTasMed(adresseData)) {
                adresseData.husnummer = sokedata.husnummer;
                adresseData.husbokstav = sokedata.husbokstav;
            }
        }
    }

    private boolean skalTasMed(AdresseData adresseData) {
        return !isBlank(adresseData.adressenavn)
                && !isBlank(adresseData.postnummer)
                && !isBlank(adresseData.poststed);
    }

    private Invocation.Builder lagRequest(RestCallContext executionContext, Sokedata sokedata, String soketype) {
        String consumerId = OidcFeatureToggleUtils.getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD");
        
        final String maxretur = (sokedata.postnummer != null) ? "100" : "10";
        WebTarget b = executionContext.getClient().target(endpoint + "adressesoek")
                .queryParam("soketype", soketype)
                .queryParam("alltidRetur", "true")
                .queryParam("maxretur", maxretur);
        
        if (sokedata.adresse != null && !sokedata.adresse.trim().equals("")) {
            b = b.queryParam("adresse", sokedata.adresse);
        }
        if (sokedata.postnummer != null) {
            b = b.queryParam("postnr", sokedata.postnummer);
        }
        if (sokedata.kommunenummer != null) {
            b = b.queryParam("kommunenr", sokedata.kommunenummer);
        }
        /* Deaktiverer husnummer i søk grunnet dårlige registerdata:
        if (sokedata.husnummer != null) {
            b = b.queryParam("husnr", sokedata.husnummer);
        }
        */
        if (isNotEmpty(apiKey)) {
            return b.request()
                    .header("Nav-Call-Id", callId)
                    .header("Nav-Consumer-Id", consumerId)
                    .header("x-nav-apiKey", apiKey);
        }
        return b.request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);
    }
}
