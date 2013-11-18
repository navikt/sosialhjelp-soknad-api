package no.nav.sbl.dialogarena.print;

import no.nav.sbl.dialogarena.print.helper.XMLTestData;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;


public class XMLGeneratorTest {

    @Ignore
    @Test
    public void generateHTMLFileDocument() throws Exception {
        String xslPath = getClass().getResource("/html/people.xsl").getPath();
        xslPath = xslPath.substring(1);
        String filePath = "c:/test/min.xml";

        Document document = XMLTestData.createDocument();
        XMLGenerator.transformToHTML(filePath, document, xslPath);
    }
}
