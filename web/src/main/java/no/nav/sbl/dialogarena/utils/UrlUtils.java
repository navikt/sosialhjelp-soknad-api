package no.nav.sbl.dialogarena.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;


public class UrlUtils {
    public static final String HOSTNAME_REGEX = "(^http.://.*?)/";

    private static final String FORTSETT_PATH = "/soknaddagpenger/utslagskriterier";

    //todo: denne bør egentlig gå til dialoginnsending slik at den er generell for alle typer søknader
    //todo: ^ Dette er egentlig fikset i foreldrepenge-soknaden (må sørge for at det blir riktig ved merge)
    public static String getFortsettUrl(String requestUrl, String behandlingId) {
        return getBaseUrl(requestUrl) + FORTSETT_PATH + "/" + behandlingId + "?utm_source=web&utm_medium=email&utm_campaign=2";
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
}

