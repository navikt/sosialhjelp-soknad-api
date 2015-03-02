package no.nav.sbl.dialogarena.sendsoknad.dialogselftest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.slf4j.LoggerFactory.getLogger;

public class SelfTestService extends SelfTestBase {

    private static final Logger logger = getLogger(SelfTestService.class);

    public SelfTestService(String applikasjonsnavn) {
        super(applikasjonsnavn);
    }

    @Override
    protected List<AvhengighetStatus> populerStatusliste() {
        List<AvhengighetStatus> liste = new ArrayList<>();
        liste.add(getAPIStatus());
        liste.addAll(getAPISelftest());
        return liste;
    }

    private AvhengighetStatus getAPIStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(System.getProperty("soknadsapi.url") + "/internal/isAlive").openConnection();
            connection.setConnectTimeout(10000);
            status = connection.getResponseCode() == HTTP_OK ? STATUS_OK : STATUS_ERROR;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting REST API isAlive", e);
        }
        return new AvhengighetStatus("REST API", status, currentTimeMillis() - start);
    }

    private List<AvhengighetStatus> getAPISelftest() {
        List<AvhengighetStatus> APISelftestStatusListe = new ArrayList<>();

        String APISelftestInnhold = lesInnholdApiSelftest();

        try {
            JSONObject jsonAPISelftest = new JSONObject(APISelftestInnhold);

            JSONArray APIAvhengigheter = jsonAPISelftest.getJSONArray("avhengigheter");

            AvhengighetStatus avhengighetStatus;
            for (int tabellRad = 0; tabellRad < APIAvhengigheter.length(); tabellRad++) {
                JSONObject jsonAPIAvhengighet = APIAvhengigheter.getJSONObject(tabellRad);
                avhengighetStatus = new AvhengighetStatus(
                        jsonAPIAvhengighet.getString("name"),
                        jsonAPIAvhengighet.getString("status"),
                        jsonAPIAvhengighet.getLong("durationMilis"),
                        jsonAPIAvhengighet.getString("beskrivelse"));
                APISelftestStatusListe.add(avhengighetStatus);
            }
        } catch (JSONException e) {
            logger.warn("<<<<<<Error ved forsøk på å opprette JSON object av API-selftest ", e);
        }
        return APISelftestStatusListe;
    }

    private String lesInnholdApiSelftest() {
        StringBuilder innhold = new StringBuilder();
        try {
            URL apiPath =  new URL(System.getProperty("soknadsapi.url") + "/internal/selftest.json");
            HttpURLConnection api = (HttpURLConnection) apiPath.openConnection();
            api.setConnectTimeout(10000);

            InputStream APISelftest = api.getInputStream();
            BufferedReader innholdLeser = new BufferedReader(new InputStreamReader(APISelftest));

            String input;
            while( (input = innholdLeser.readLine()) != null ){
                innhold.append(input);
            }
            innholdLeser.close();

        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting REST API Selftest", e);
        }
        return  innhold.toString();
    }
}
