package no.nav.sbl.dialogarena.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;

public class UrlUtils {
    public static final String HOSTNAME_REGEX = "(^http.://.*?)/";

    public static String getStartDagpengerUrl() {
        return getProperty("soknad.dagpenger.fortsett.path") + "/start";
    }

    public static String getFortsettUrl(String behandlingId) {
        return getProperty("soknadinnsending.link.url") + "/soknad/" + behandlingId + "?utm_source=web&utm_medium=email&utm_campaign=2";
    }

    public static String getEttersendelseUrl(String requestUrl, String behandlingId) {
        return getBaseUrl(requestUrl) + getProperty("soknadinnsending.ettersending.path") + "/" + behandlingId;
    }

    private static String getBaseUrl(String requestUrl) {
        Matcher matcher = Pattern.compile(HOSTNAME_REGEX).matcher(requestUrl);
        String baseUrl = "";
        if (matcher.find()) {
            baseUrl = matcher.group(1);
        }
        return baseUrl;
    }


    public static String endreHyperLenkerTilTekst(String html) {

        if (html == null || html.isEmpty()) {
            return html;
        }

        Pattern p = Pattern.compile(HTMLLinkParser.HTML_A_TAG_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);


        while (m.find()) {
            ArrayList<HTMLLinkParser.HtmlLink> links = HTMLLinkParser.getLinks(m.group());
            html = html.replace(m.group(), links.get(0).toString()).trim();

        }
        return html;
    }

}



