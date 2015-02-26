package no.nav.sbl.dialogarena.selftest;

import no.nav.sbl.dialogarena.soknadinnsending.business.selftest.AvhengighetStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.selftest.SelfTestService;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.net.InetAddress.getLocalHost;
import static no.nav.sbl.dialogarena.soknadinnsending.business.selftest.SelfTestService.STATUS_ERROR;
import static no.nav.sbl.dialogarena.soknadinnsending.business.selftest.SelfTestService.STATUS_OK;
import static org.apache.commons.lang3.StringUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

public class SelfTest {

    private static final Logger logger = getLogger(SelfTest.class);
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected String version = "Unknown";
    protected String oppsummertStatus;
    protected String message;
    protected List<AvhengighetStatus> statusList = new ArrayList<>();
    private static final String APPLIKASJONS_NAVN = "Søknadsapi";

    @Inject
    private SelfTestService selfTestService;


    public String asHtml(ServletContext context) {
        setVersion(context);
        statusList = selfTestService.hentStatusliste();
        sjekkOppsummertStatus();
        return hentHtmlContent();
    }

    public Map<String, Object> asJson(ServletContext context) {
        setVersion(context);
        statusList = selfTestService.hentStatusliste();
        sjekkOppsummertStatus();
        return hentJsonContent();
    }


    /**
     * Override ved behov for ekstra info i selftest
     */
    protected String extraHtml() {
        return "";
    }

    private String hentHtmlContent() {
        SelfTestHTML html = new SelfTestHTML(APPLIKASJONS_NAVN + " selftest");

        html.appendToBody("h1", "Service status: " + oppsummertStatus);
        html.appendToBody("h3", APPLIKASJONS_NAVN + " - " + version);
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
        content.put("navn", APPLIKASJONS_NAVN + "selftest");
        content.put("host", getHost());
        content.put("version", version);
        content.put("status", oppsummertStatus);
        content.put("message", message);
        content.put("avhengigheter", statusList);

        return content;
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

        public void appendToBody(List list) {

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
                AvhengighetStatus serviceStatus = (AvhengighetStatus) iterator.next();
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

    private String getHost() {
        try {
            return getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.error("Error retrieving host", e);
            return "unknown host";
        }
    }

    private void setVersion(ServletContext context) {

        try {

            try (InputStream inputStream = context.getResourceAsStream(("/META-INF/MANIFEST.MF"))) {
                Manifest manifest = new Manifest(inputStream);
                version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION.toString());
            }
        } catch (Exception e) {
            logger.error("Unable to fetch the application version", e);
            version = "Unknown";
        }
    }


}
