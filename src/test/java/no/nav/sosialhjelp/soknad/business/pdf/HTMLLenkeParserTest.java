package no.nav.sosialhjelp.soknad.business.pdf;

import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


public class HTMLLenkeParserTest {


    private static String TEST_LENKE = "http://www.nav.no";


    @Test
    public void testHTMLLenkeParser() {

        String html = "abc hmmmm <a href='" + TEST_LENKE + "'>NAV</a>" +
                "\n" + "abc hmmmm <a HREF='" + TEST_LENKE + "'>NAV</a>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LENKE + "'>NAV</A> abc hmmmm <A HREF='" + TEST_LENKE + "' + target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A HREF='" + TEST_LENKE + "' target='_blank'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF='" + TEST_LENKE + "'>NAV</A>" +
                "\n" + "abc hmmmm <A target='_blank' HREF=\"" + TEST_LENKE + "\">NAV</A>" +
                "\n" + "abc hmmmm <a HREF=" + TEST_LENKE + ">NAV</a>";

        ArrayList<HTMLLenkeParser.HTMLLenke> lenker = HTMLLenkeParser.hentLenker(html);

        assertThat(lenker).isNotEmpty();

        for (HTMLLenkeParser.HTMLLenke htmlLenker : lenker) {
            assertThat(htmlLenker.getLenke()).isEqualTo(TEST_LENKE);
        }
    }
    @Test
    public void testSplitByNumber() {
        String langUrL = "https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-av-vedleggene-med-mobilen";

        String[] strings = HTMLLenkeParser.splittLinjeEtterAntallTegn(langUrL, 50);

        langUrL = String.join("<br />", strings);

        assertThat(langUrL).contains("<br />");
    }
}