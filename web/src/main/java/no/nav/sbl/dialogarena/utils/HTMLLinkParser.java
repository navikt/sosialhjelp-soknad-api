package no.nav.sbl.dialogarena.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLLinkParser {

    public static Pattern patternTag, patternLink;
    private static Matcher matcherTag, matcherLink;

    public static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_A_HREF_TAG_PATTERN =
            "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";


    public static ArrayList<HTMLLinkParser.HtmlLink> getLinks(final String html) {
        patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
        patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);


        return HTMLLinkParser.extractHTMLLinks(html);

    }

    private static ArrayList<HtmlLink> extractHTMLLinks(final String html) {

        ArrayList<HtmlLink> result = new ArrayList<>();

        matcherTag = patternTag.matcher(html);

        while (matcherTag.find()) {

            String href = matcherTag.group(1); // href
            String linkText = matcherTag.group(2); // link text

            matcherLink = patternLink.matcher(href);

            while (matcherLink.find()) {

                String link = matcherLink.group(1); // link
                HtmlLink htmlLink = new HtmlLink();
                htmlLink.setLink(link);
                htmlLink.setLinkText(linkText);

                result.add(htmlLink);

            }

        }

        return result;

    }

    static class HtmlLink {

        private String link;
        private String linkText;


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