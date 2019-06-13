package no.nav.sbl.sosialhjelp.pdf;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLLenkeParser {

    public static Pattern patternTag, patternLenke;
    private static Matcher matcherTag, matcherLenke;

    public static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_A_HREF_TAG_PATTERN =
            "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


    public static ArrayList<HTMLLenke> hentLenker(final String html) {

        patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
        patternLenke = Pattern.compile(HTML_A_HREF_TAG_PATTERN);

        return HTMLLenkeParser.ekstraherHTMLLenker(html);

    }

    public static String[] splittLinjeEtterAntallTegn(String linje, int antallTegn) {
        return (antallTegn < 1 || linje == null) ? null : linje.split("(?<=\\G.{" + antallTegn + "})");
    }

    private static ArrayList<HTMLLenke> ekstraherHTMLLenker(final String html) {

        ArrayList<HTMLLenke> resultat = new ArrayList<>();

        matcherTag = patternTag.matcher(html);

        while (matcherTag.find()) {

            String href = matcherTag.group(1); // href
            String linkText = matcherTag.group(2); // lenke text

            matcherLenke = patternLenke.matcher(href);

            while (matcherLenke.find()) {

                String link = matcherLenke.group(1); // lenke
                HTMLLenke htmlLink = new HTMLLenke();
                htmlLink.setLenke(link);
                htmlLink.setLenkeTekst(linkText);

                resultat.add(htmlLink);

            }

        }

        return resultat;

    }

    static class HTMLLenke {

        private String lenke;
        private String lenkeTekst;


        @Override
        public String toString() {

            int length = 80;
            if (this.getLenke().length() > length) {
                String[] tekststrenger = splittLinjeEtterAntallTegn(this.getLenke(), length);
                this.setLenke((String.join("<br />", tekststrenger)));

            }
            return new StringBuffer().append(this.lenkeTekst)
                    .append("\n(").append(this.lenke).append(")").toString().trim();
        }

        public String getLenke() {
            return lenke;
        }

        public void setLenke(String lenke) {
            this.lenke = erstattInvalideTegn(lenke);
        }

        public String getLenkeTekst() {
            return lenkeTekst;
        }

        public void setLenkeTekst(String lenkeTekst) {
            this.lenkeTekst = lenkeTekst;
        }

        private String erstattInvalideTegn(String lenke) {
            lenke = lenke.replaceAll("'", "");
            lenke = lenke.replaceAll("\"", "");
            return lenke;
        }
    }
}