package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ArbeidssokerInfoService {
    @Value("${arbeid.rest.url}")
    private String sblArbeidBaseUrl;

    @Value("${arbeidservice.username}")
    private String username;

    @Value("${arbeidservice.password}")
    private String password;

    final String UKJENT = "UKJENT";

    private final Logger logger = getLogger(ArbeidssokerInfoService.class);
    private HttpClient httpclient = HttpClients.createDefault();

    public String getArbeidssokerRegistreringStatus(String fnr) {
        String authString = username + ":" + password;
        String encodedAuth = encodeBase64String(authString.getBytes());

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        HttpGet httpget = new HttpGet(sblArbeidBaseUrl + "bruker/fnr/" + fnr);
        httpget.setHeader("Authorization", String.format("Basic %s", encodedAuth));

        try {
            HttpResponse response = httpclient.execute(httpget);
            return hentStatusFraResponse(responseHandler.handleResponse(response));
        } catch (IOException e) {
            logger.error("Feil ved henting av status fra SBL Arbeid for fnr {}: {}", fnr, e);
            return UKJENT;
        }
    }

    private String hentStatusFraResponse(String response) {
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        JsonElement jArbStatus = jObject.get("brukerStatus");
        return jArbStatus == null ? UKJENT : jArbStatus.getAsString();
    }
}
