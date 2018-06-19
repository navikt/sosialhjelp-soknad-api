package no.nav.sbl.dialogarena.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Vector;

/**
 * HTML link extrator Testing
 *
 * @author mkyong
 */
class TestHTMLLinkExtractor {

    private HTMLLinkExtractor htmlLinkExtractor;
    String TEST_LINK = "http://www.google.com";

    @BeforeClass
    public void initData() {
        htmlLinkExtractor = new HTMLLinkExtractor();
    }


    public Object[][] HTMLContentProvider() {
        return new Object[][]{
                new Object[]{"abc hahaha <a href='" + TEST_LINK + "'>google</a>"},
                new Object[]{"abc hahaha <a HREF='" + TEST_LINK + "'>google</a>"},

                new Object[]{"abc hahaha <A HREF='" + TEST_LINK + "'>google</A> , "
                        + "abc hahaha <A HREF='" + TEST_LINK + "' target='_blank'>google</A>"},

                new Object[]{"abc hahaha <A HREF='" + TEST_LINK + "' target='_blank'>google</A>"},
                new Object[]{"abc hahaha <A target='_blank' HREF='" + TEST_LINK + "'>google</A>"},
                new Object[]{"abc hahaha <A target='_blank' HREF=\"" + TEST_LINK + "\">google</A>"},
                new Object[]{"abc hahaha <a HREF=" + TEST_LINK + ">google</a>"},};
    }

    @Test
    public void ValidHTMLLinkTest(String html) {

        Vector<HTMLLinkExtractor.HtmlLink> links = htmlLinkExtractor.grabHTMLLinks(html);

        //there must have something
        Assert.assertTrue(links.size() != 0);

        for (int i = 0; i < links.size(); i++) {
            HTMLLinkExtractor.HtmlLink htmlLinks = links.get(i);
            //System.out.println(htmlLinks);
            Assert.assertEquals(htmlLinks.getLink(), TEST_LINK);
        }

    }
}