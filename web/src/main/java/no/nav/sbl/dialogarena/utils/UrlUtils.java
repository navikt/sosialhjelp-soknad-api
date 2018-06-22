package no.nav.sbl.dialogarena.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        Pattern p = Pattern.compile(HTMLLenkeParser.HTML_A_TAG_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);


        while (m.find()) {
            ArrayList<HTMLLenkeParser.HTMLLenke> lenker = HTMLLenkeParser.hentLenker(m.group());

            if (lenker != null && lenker.get(0) != null) {

                HTMLLenkeParser.HTMLLenke lenke = lenker.get(0);
                int length = 80;
                if (lenke.getLenke().length() > length) {
                    String[] tekststrenger = UrlUtils.splittLinjeEtterAntallTegn(lenke.getLenke(), length);
                    lenke.setLenke((Arrays.stream(tekststrenger).collect(Collectors.joining("<br />"))));
                }

                html = html.replace(m.group(), lenke.toString()).trim();
            }

        }
        return html;
    }

    public static String[] splittLinjeEtterAntallTegn(String linje, int antallTegn) {
        return (antallTegn < 1 || linje == null) ? null : linje.split("(?<=\\G.{" + antallTegn + "})");
    }
}



