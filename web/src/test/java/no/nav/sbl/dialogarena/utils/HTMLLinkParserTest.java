package no.nav.sbl.dialogarena.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;


public class HTMLLinkParserTest {

    private static HTMLLinkParser htmlLinkExtractor;

    private static String TEST_LINK = "http://www.nav.no";

    @BeforeClass
    public void setup() {

        htmlLinkExtractor = new HTMLLinkParser();
    }


    @Test
    public void testValidHTML() {

        String html = HTMLContentProvider().toString();

        ArrayList<HTMLLinkParser.HtmlLink> links = htmlLinkExtractor.extractHTMLLinks(html);

        Assert.assertTrue(links.size() != 0);

        for (int i = 0; i < links.size(); i++) {
            HTMLLinkParser.HtmlLink htmlLinks = links.get(i);

            Assert.assertEquals(htmlLinks.getLink(), TEST_LINK);
        }

    }

    private Object[][] HTMLContentProvider() {
        return new Object[][]{
                new Object[]{"abc hmmmm <a href='" + TEST_LINK + "'>NAV</a>"},
                new Object[]{"abc hmmmm <a HREF='" + TEST_LINK + "'>NAV</a>"},

                new Object[]{"abc hmmmm <A HREF='" + TEST_LINK + "'>NAV</A> , "
                        + "abc hmmmm <A HREF='" + TEST_LINK + "' target='_blank'>NAV</A>"},

                new Object[]{"abc hmmmm <A HREF='" + TEST_LINK + "' target='_blank'>NAV</A>"},
                new Object[]{"abc hmmmm <A target='_blank' HREF='" + TEST_LINK + "'>NAV</A>"},
                new Object[]{"abc hmmmm <A target='_blank' HREF=\"" + TEST_LINK + "\">NAV</A>"},
                new Object[]{"abc hmmmm <a HREF=" + TEST_LINK + ">NAV</a>"},};
    }
}