package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.slf4j.Logger;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class SelfTestBase {

    private static final Logger logger = getLogger(SelfTestBase.class);
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected static final String STATUS_OK = "OK";
    protected static final String STATUS_ERROR = "ERROR";

    protected String version = getApplicationVersion();
    protected String host = this.getHost();
    protected String oppsummertStatus;
    protected String message;
    protected ArrayList<AvhengighetStatus> statusList = new ArrayList();

    public void sjekkOppsummertStatus() {
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

    public class SelfTestHTML {

        private String title;
        private String newLine = System.getProperty("line.separator");
        private StringBuilder header = new StringBuilder();
        private StringBuilder style = new StringBuilder();
        private StringBuilder body = new StringBuilder();

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
            style.append("<style type=\"text/css\">");
            style.append("table{" +
                    "background-color: #f0f8ff;" +
                    " border-collapse: collapse;" +
                    "}");
            style.append(" td, th {" +
                    "border: 1px #888 solid;" +
                    "border-collapse: collapse;" +
                    "padding: 1px 5px 1px 5px;" +
                    "}");
            style.append("</style>");

            return style.toString();
        }

        private String buildHeader() {
            header.append("<!DOCTYPE html>" + newLine);
            header.append("<html lang=\"no\">" + newLine);
            header.append("<head>" + newLine);
            header.append("<title>" + title + "</title>" + newLine);
            header.append(buildStyle() + newLine + "</head>" + newLine);
            return header.toString();
        }

        private String buildFooter() {
            return "</html>" + newLine;
        }

        private String buildBody() {
            return "<body>" + newLine +
                    body.toString() + newLine +
                    "</body>" + newLine;
        }


        public void appendToBody(String tag, String text) {
            String str = "<%tag%>%text%</%tag%>".
                    replace("%tag%", tag).
                    replace("%text%", text);
            body.append(str + newLine);
        }

        public void appendToBody(ArrayList list) {

            body.append("<table>" + newLine);
            body.append("<table>" + newLine);
            body.append("<tr>" + newLine);
            body.append("<th>Status</th>" + newLine);
            body.append("<th>Navn</th>" + newLine);
            body.append("<th>Responstid ms</th>" + newLine);
            body.append("<th>Beskrivelse</th>" + newLine);
            body.append("</tr>" + newLine);

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                SelfTestBase.AvhengighetStatus serviceStatus = (SelfTestBase.AvhengighetStatus) iterator.next();
                body.append("<tr>" + newLine);
                body.append("<td>" + serviceStatus.getStatus() + "</td>" + newLine);
                body.append("<td>" + serviceStatus.getName() + "</td>" + newLine);
                body.append("<td>" + serviceStatus.getDurationMilis() + "</td>" + newLine);
                body.append("<td>" + serviceStatus.getBeskrivelse() + "</td>" + newLine);
                body.append("</tr>" + newLine);
            }
            body.append("</table>" + newLine);

        }

    }

    protected String getApplicationVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.error("Error retrieving host", e);
            return "unknown host";
        }
    }

    public static class AvhengighetStatus implements Serializable {
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
