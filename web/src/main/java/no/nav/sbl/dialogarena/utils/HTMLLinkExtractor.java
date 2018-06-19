package no.nav.sbl.dialogarena.utils;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLLinkExtractor {

    public static Pattern patternTag, patternLink;
    private static Matcher matcherTag, matcherLink;

    public static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_A_HREF_TAG_PATTERN =
            "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


    public static Vector<HTMLLinkExtractor.HtmlLink> getLinks(final String html) {
        patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
        patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);


        return HTMLLinkExtractor.grabHTMLLinks(html);

    }

    /**
     * Validate html with regular expression
     *
     * @param html html content for validation
     * @return Vector links and link text
     */
    public static Vector<HtmlLink> grabHTMLLinks(final String html) {

        Vector<HtmlLink> result = new Vector<HtmlLink>();

        matcherTag = patternTag.matcher(html);

        while (matcherTag.find()) {

            String href = matcherTag.group(1); // href
            String linkText = matcherTag.group(2); // link text

            matcherLink = patternLink.matcher(href);

            while (matcherLink.find()) {

                String link = matcherLink.group(1); // link
                HtmlLink obj = new HtmlLink();
                obj.setLink(link);
                obj.setLinkText(linkText);

                result.add(obj);

            }

        }

        return result;

    }

    static class HtmlLink {

        String link;
        String linkText;

        HtmlLink() {
        }

        ;

        @Override
        public String toString() {
            return new StringBuffer().append(this.linkText)
                    .append("(").append(this.link).append(")").toString().trim();
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = replaceInvalidChar(link);
        }

        public String getLinkText() {
            return linkText;
        }

        public void setLinkText(String linkText) {
            this.linkText = linkText;
        }

        private String replaceInvalidChar(String link) {
            link = link.replaceAll("'", "");
            link = link.replaceAll("\"", "");
            return link;
        }

    }
}