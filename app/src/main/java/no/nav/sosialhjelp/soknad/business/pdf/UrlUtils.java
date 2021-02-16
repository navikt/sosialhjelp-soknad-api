package no.nav.sosialhjelp.soknad.business.pdf;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

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
                html = html.replace(m.group(), lenke.toString()).trim();
            }
        }
        return html;
    }
}



