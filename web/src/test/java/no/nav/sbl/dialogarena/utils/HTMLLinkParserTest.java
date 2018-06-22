package no.nav.sbl.dialogarena.utils;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class HTMLLinkParserTest {


    private static String TEST_LINK = "http://www.nav.no";


    @Test
    public void testValidHTML() {

        HTMLLinkParser htmlLinkExtractor = new HTMLLinkParser();

        String html = "abc hmmmm <a href='" + TEST_LINK + "'>NAV</a>" +
                "\n" + "abc hmmmm <a HREF='" + TEST_LINK + "'>NAV</a>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LINK + "'>NAV</A> abc hmmmm <A HREF='" + TEST_LINK + "' + target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LINK + "' target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF='" + TEST_LINK + "'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF=\"" + TEST_LINK + "\">NAV</A>" +
                "\n" + "abc hmmmm <a HREF=" + TEST_LINK + ">NAV</a>";



        ArrayList<HTMLLinkParser.HTMLLenke> links = htmlLinkExtractor.hentLenker(html);

        assertTrue(links.size() != 0);

        for (int i = 0; i < links.size(); i++) {
            HTMLLinkParser.HTMLLenke htmlLinks = links.get(i);

            assertEquals(htmlLinks.getLenke(), TEST_LINK);
        }
    }
}