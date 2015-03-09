package no.nav.sbl.dialogarena.sendsoknad.dialogselftest;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;
import static java.net.InetAddress.getLocalHost;
import static org.apache.commons.lang3.StringUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class SelfTestBase {

    private static final Logger logger = getLogger(SelfTestBase.class);
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected static final String STATUS_OK = "OK";
    protected static final String STATUS_ERROR = "ERROR";

    protected String version = "TODO fix versjon";
    protected String oppsummertStatus;
    protected String message;
    protected String applikasjonsnavn;
    protected List<AvhengighetStatus> statusList = new ArrayList();

    public SelfTestBase(String applikasjonsnavn) {
        this.applikasjonsnavn = applikasjonsnavn;
    }

    public String asHtml() {
        updateStatusList();
        sjekkOppsummertStatus();
        return hentHtmlContent();
    }

    public Map<String, Object> asJson() {
        updateStatusList();
        sjekkOppsummertStatus();
        return hentJsonContent();
    }

    protected abstract List<AvhengighetStatus> populerStatusliste();

    /**
     * Override ved behov for ekstra info i selftest
     */
    protected String extraHtml() {
        return "";
    }

    private String hentHtmlContent() {
        SelfTestHTML html = new SelfTestHTML(applikasjonsnavn + " selftest");

        html.appendToBody("h1", "Service status: " + oppsummertStatus);
        html.appendToBody("h3", applikasjonsnavn + " - " + version);
        html.appendToBody("h3", getHost());
        html.appendToBody(statusList);
        html.appendToBody("h5", "Siden generert: " + LocalDateTime.now().toString(DATE_FORMAT));
        html.appendToBody("h6", message);
        if (!extraHtml().isEmpty()) {
            html.appendToBody("p", extraHtml());
        }

        return html.buildPage();
    }

    private Map<String, Object> hentJsonContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("navn", applikasjonsnavn + "selftest");
        content.put("host", getHost());
        content.put("version", version);
        content.put("status", oppsummertStatus);
        content.put("message", message);
        content.put("avhengigheter", statusList);

        return content;
    }

    private void updateStatusList() {
        statusList = populerStatusliste();
    }

    private void sjekkOppsummertStatus() {
        ArrayList<String> failed = new ArrayList<>();
        oppsummertStatus = STATUS_OK;
        for (AvhengighetStatus avhengighetStatus : statusList) {
            if (!STATUS_OK.equals(avhengighetStatus.getStatus())) {
                oppsummertStatus = STATUS_ERROR;
                failed.add(avhengighetStatus.getName());
            }
        }
        message = join(failed, ',');
        if (STATUS_ERROR.equals(oppsummertStatus)) {
            logger.error("Selftest rapporterer melding: " + message);
        }
    }

    private class SelfTestHTML {

        private String title;
        private String newLine = getProperty("line.separator");
        private String style;
        private String body = "";

        public SelfTestHTML(String title) {
            this.title = title;
        }

        public String buildPage() {
            String header = buildHeader();
            String body = buildBody();
            String footer = buildFooter();

            return header + style + body + footer;
        }

        private String buildStyle() {
            StringBuilder styleBuilder = new StringBuilder();
            styleBuilder.append("<style type=\"text/css\">");
            styleBuilder.append("table{" +
                    "background-color: #f0f8ff;" +
                    " border-collapse: collapse;" +
                    "}");
            styleBuilder.append(" td, th {" +
                    "border: 1px #888 solid;" +
                    "border-collapse: collapse;" +
                    "padding: 1px 5px 1px 5px;" +
                    "}");
            styleBuilder.append("</style>");

            String styleResult = styleBuilder.toString();
            style = styleResult;
            return styleResult;
        }

        private String buildHeader() {
            return ""
                    .concat("<!DOCTYPE html>" + newLine)
                    .concat("<html lang=\"no\">" + newLine)
                    .concat("<head>" + newLine)
                    .concat("<title>" + title + "</title>" + newLine)
                    .concat(buildStyle() + newLine + "</head>" + newLine);
        }

        private String buildFooter() {
            return "</html>" + newLine;
        }

        private String buildBody() {
            return "<body>" + newLine +
                    body + newLine +
                    "</body>" + newLine;
        }


        public void appendToBody(String tag, String text) {
            String str = "<%tag%>%text%</%tag%>".
                    replace("%tag%", tag).
                    replace("%text%", text);
            body.concat(str + newLine);
        }

        public void appendToBody(List list) {

            body.concat("<table>" + newLine);
            body.concat("<table>" + newLine);
            body.concat("<tr>" + newLine);
            body.concat("<th>Status</th>" + newLine);
            body.concat("<th>Navn</th>" + newLine);
            body.concat("<th>Responstid ms</th>" + newLine);
            body.concat("<th>Beskrivelse</th>" + newLine);
            body.concat("</tr>" + newLine);

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                SelfTestBase.AvhengighetStatus serviceStatus = (SelfTestBase.AvhengighetStatus) iterator.next();
                body.concat("<tr>" + newLine);
                body.concat("<td>" + serviceStatus.getStatus() + "</td>" + newLine);
                body.concat("<td>" + serviceStatus.getName() + "</td>" + newLine);
                body.concat("<td>" + serviceStatus.getDurationMilis() + "</td>" + newLine);
                body.concat("<td>" + serviceStatus.getBeskrivelse() + "</td>" + newLine);
                body.concat("</tr>" + newLine);
            }
            body.concat("</table>" + newLine);
        }

    }

    private String getHost() {
        try {
            return getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.error("Error retrieving host", e);
            return "unknown host";
        }
    }

    protected static class AvhengighetStatus implements Serializable {
        private final String navn;
        private final String status;
        private final long durationMilis;
        private final String beskrivelse;

        public AvhengighetStatus(String name, String status, long durationMilis, String beskrivelse) {
            this.navn = name;
            this.status = status;
            this.durationMilis = durationMilis;
            this.beskrivelse = beskrivelse;
        }

        public AvhengighetStatus(String name, String status, long durationMilis) {
            this(name, status, durationMilis, "");
        }

        public String getName() {
            return this.navn;
        }

        public String getStatus() {
            return this.status;
        }

        public long getDurationMilis() {
            return this.durationMilis;
        }

        public String getBeskrivelse() {
            return this.beskrivelse;
        }
    }

}
