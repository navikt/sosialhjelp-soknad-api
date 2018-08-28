package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import static java.lang.System.getenv;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;

public class AdresseSokConsumerImpl implements AdresseSokConsumer {

    private static final Logger logger = getLogger(AdresseSokConsumerImpl.class);

    private Client client;
    private String endpoint;

    public AdresseSokConsumerImpl(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @Override
    public AdressesokRespons sokAdresse(String adresse) {
        final Sokedata sokedata = AdresseStringSplitter.toSokedata(adresse);
        return sokAdresse(sokedata);
    }
    
    @Override
    public AdressesokRespons sokAdresse(Sokedata sokedata) {
        final AdressesokRespons respons = sokAdresseCall(sokedata, "E");
        if (!respons.adresseDataList.isEmpty()) {
            return respons;
        }
        
        return sokAdresseCall(sokedata, "F");
    }
    
    private AdressesokRespons sokAdresseCall(Sokedata sokedata, String soketype) {
        final Invocation.Builder request = lagRequest(sokedata, soketype);
        Response response = null;

        try {
            response = request.get();
            
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
        } finally {
            if (response != null) {
                response.close();
            }
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
        return !isEmpty(adresseData.adressenavn)
                && !isEmpty(adresseData.postnummer)
                && !isEmpty(adresseData.poststed);
    }
    
    private static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private Invocation.Builder lagRequest(Sokedata sokedata, String soketype) {
        String consumerId = getSubjectHandler().getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        final String apiKey = getenv("SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD");
        
        final String maxretur = (sokedata.postnummer != null) ? "100" : "10";
        WebTarget b = client.target(endpoint + "adressesoek")
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
        if (sokedata.husnummer != null) {
            b = b.queryParam("husnr", sokedata.husnummer);
        }
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
