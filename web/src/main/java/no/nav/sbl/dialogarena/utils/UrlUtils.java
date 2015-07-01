package no.nav.sbl.dialogarena.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;


public class UrlUtils {
    public static final String HOSTNAME_REGEX = "(^http.://.*?)/";

    public static String getStartDagpengerUrl() {
        return getProperty("soknad.dagpenger.fortsett.path") + "/start" ;
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
}

