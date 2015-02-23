package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;


import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SelfTestService extends SelfTestBase {

    private static final Logger logger = getLogger(SelfTestService.class);


    public SelfTestService() {
        this("Søknads api");
    }

    public SelfTestService(String applikasjonsNavn) {
        super(applikasjonsNavn);
    }

    public String getAsHTML() {
        this.replaceStatusList(statusList);
        html = new SelfTestHTML(applikasjonsNavn + " selftest");

        html.appendToBody("h1", "Service status: " + status);
        html.appendToBody("h3", applikasjonsNavn + " - " + version);
        html.appendToBody("h3", host);
        html.appendToBody(statusList);
        html.appendToBody("h5", "Siden generert: " + LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss"));

        html.appendToBody("h6", message);

        return html.buildPage();
    }

    private AvhengighetStatus getCMSStatus() {
        long start = currentTimeMillis();
        String status = STATUS_ERROR;
        String url = "";
        HttpURLConnection connection = null;
        try {
            url = System.getProperty("dialogarena.cms.url");
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            status = connection.getResponseCode() == HTTP_OK ? STATUS_OK : STATUS_ERROR;
        } catch (IOException e) {
            logger.warn("CMS not reachable on " + url , e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new AvhengighetStatus("ENONIC_APPRES", status, currentTimeMillis() - start, format("URL: %s", url));
    }


    protected void replaceStatusList(List<AvhengighetStatus> statusList) {
        statusList.clear();
        statusList.addAll(asList(
                getCMSStatus()
        ));
    }

    public Map<String, Object> getAsJSON() {
        this.replaceStatusList(statusList);
        Map<String, Object> content = new HashMap<>();
        content.put("navn", applikasjonsNavn + "selftest");
        content.put("host", this.host);
        content.put("version", this.version);
        content.put("status", this.status);
        content.put("message", this.message);
        content.put("avhengigheter", this.statusList);

        return content;
    }


}
