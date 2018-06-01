package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseStringSplitter.Adressefelter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import org.slf4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

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
        final Adressefelter adressefelter = AdresseStringSplitter.toAdressefelter(adresse);
        final Invocation.Builder request = lagRequest(adressefelter);
        Response response = null;

        try {
            response = request.get();
            if (response.getStatus() == 200) {
                return createAdressesokRespons(adressefelter, response);
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

    private AdressesokRespons createAdressesokRespons(Adressefelter adressefelter, Response response) {
        final AdressesokRespons result = response.readEntity(AdressesokRespons.class);
        taMedDataFraRequest(adressefelter, result);
        return result;
    }

    private void taMedDataFraRequest(Adressefelter adressefelter, AdressesokRespons result) {
        for (AdresseData adresseData : result.adresseDataList) {
            adresseData.husnummer = adressefelter.husnummer;
            adresseData.husbokstav = adressefelter.husbokstav;
        }
    }

    private Invocation.Builder lagRequest(Adressefelter adressefelter ) {
        String consumerId = getSubjectHandler().getConsumerId();
        String callId = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        
        WebTarget b = client.target(endpoint + "/adressesoek")
                .queryParam("soketype", "E")
                .queryParam("alltidRetur", "true")
                .queryParam("maxretur", "100");
        
        if (adressefelter.adresse != null) {
            b = b.queryParam("adresse", adressefelter.adresse);
        }
        if (adressefelter.postnummer != null) {
            b = b.queryParam("postnr", adressefelter.postnummer);
        }
        return b.request()
                .header("Nav-Call-Id", callId)
                .header("Nav-Consumer-Id", consumerId);
    }


}
