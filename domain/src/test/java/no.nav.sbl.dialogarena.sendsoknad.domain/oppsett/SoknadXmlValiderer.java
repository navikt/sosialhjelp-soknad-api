package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import com.google.common.io.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.junit.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import java.util.*;

import static junit.framework.Assert.*;

public class SoknadXmlValiderer {

    @Test
    public void testDagpengerXml() throws Exception {
        testOmXmlValiderer("dagpenger/dagpenger_ordinaer.xml");
    }

    @Test
    public void testDagpengerGjenopptakXml() throws Exception {
        testOmXmlValiderer("dagpenger/dagpenger_gjenopptak.xml");
    }

    @Test
    public void testForeldrepengerXml() throws Exception {
        testOmXmlValiderer("foreldresoknad.xml");
    }

    @Test
    public void testTilleggStonaderXml() throws Exception {
        testOmXmlValiderer("soknadtilleggsstonader.xml");
    }

    @Test
    public void testRefusjonXml() throws Exception {
        testOmXmlValiderer("refusjondagligreise.xml");
    }

    @Test
    public void testTiltakspengerXml() throws Exception {
        testOmXmlValiderer("tiltakspenger.xml");
    }

    @Test
    public void testBilstonadXml() throws Exception {
        testOmXmlValiderer("bilstonad.xml");
    }

    @Test
    public void testAapXml() throws Exception {
        testOmXmlValiderer("aap_ordinaer.xml");
    }

    private void testOmXmlValiderer(String xmlFilNavn) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("soknader/soknadstruktur.xsd")));
        Validator validator = schema.newValidator();

        StreamSource xmlSource = new XmlService().lastXmlFil("soknader/" + xmlFilNavn);

        try {
            validator.validate(xmlSource);
        } catch (SAXParseException e) {
            e.printStackTrace();

            List<String> xmlLines = CharStreams.readLines(new XmlService().lastXmlFil("soknader/" + xmlFilNavn).getReader());
            for (int i = 0; i < xmlLines.size(); i++) {
                String xmlLine = xmlLines.get(i);
                System.out.println(String.format("%5d: %s", i, xmlLine));
            }

            fail();
        }
    }

}
