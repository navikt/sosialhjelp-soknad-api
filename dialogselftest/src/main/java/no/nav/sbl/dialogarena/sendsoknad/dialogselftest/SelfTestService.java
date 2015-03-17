package no.nav.sbl.dialogarena.sendsoknad.dialogselftest;

import org.slf4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
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
        liste.add(getEnonicStatus());
        return liste;
    }

    private AvhengighetStatus getEnonicStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(getProperty("soknadsapi.url") + "/internal/isAlive").openConnection();
            connection.setConnectTimeout(10000);
            status = connection.getResponseCode() == HTTP_OK ? STATUS_OK : STATUS_ERROR;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting REST API isAlive", e);
        }
        return new AvhengighetStatus("REST API", status, currentTimeMillis() - start);
    }

    private AvhengighetStatus getAPIStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(getProperty("soknadsapi.url") + "/internal/isAlive").openConnection();
            connection.setConnectTimeout(10000);
            status = connection.getResponseCode() == HTTP_OK ? STATUS_OK : STATUS_ERROR;
        } catch (Exception e) {
            logger.warn("<<<<<<Error Contacting REST API isAlive", e);
        }
        return new AvhengighetStatus("REST API", status, currentTimeMillis() - start);
    }

}
