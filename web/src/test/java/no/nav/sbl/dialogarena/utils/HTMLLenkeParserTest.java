package no.nav.sbl.dialogarena.utils;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class HTMLLenkeParserTest {


    private static String TEST_LENKE = "http://www.nav.no";


    @Test
    public void testHTMLLenkeParser() {

        HTMLLenkeParser htmlLenkeParser = new HTMLLenkeParser();

        String html = "abc hmmmm <a href='" + TEST_LENKE + "'>NAV</a>" +
                "\n" + "abc hmmmm <a HREF='" + TEST_LENKE + "'>NAV</a>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LENKE + "'>NAV</A> abc hmmmm <A HREF='" + TEST_LENKE + "' + target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LENKE + "' target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF='" + TEST_LENKE + "'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF=\"" + TEST_LENKE + "\">NAV</A>" +
                "\n" + "abc hmmmm <a HREF=" + TEST_LENKE + ">NAV</a>";



        ArrayList<HTMLLenkeParser.HTMLLenke> lenker = htmlLenkeParser.hentLenker(html);

        assertTrue(lenker.size() != 0);

        for (int i = 0; i < lenker.size(); i++) {
            HTMLLenkeParser.HTMLLenke htmlLenker = lenker.get(i);

            assertEquals(htmlLenker.getLenke(), TEST_LENKE);
        }
    }
}