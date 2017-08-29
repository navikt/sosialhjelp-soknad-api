package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import com.google.common.io.CharStreams;
import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.Assert.fail;

public class SoknadXmlValidererTest {

    @BeforeClass
    public static void genererXsd() throws JAXBException, IOException {
        SoknadStrukturXsdGenerator.genererSkjema();
    }

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
        testOmXmlValiderer("foreldrepenger/foreldrepenger.xml");
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
    public void testAapOrdinaerXml() throws Exception {
        testOmXmlValiderer("aap/aap_ordinaer.xml");
    }

    @Test
    public void testAapGjenopptakXml() throws Exception {
        testOmXmlValiderer("aap/aap_gjenopptak.xml");
    }

    @Test
    public void testSosialhjelpXml() throws Exception {
        testOmXmlValiderer("sosialhjelp/sosialhjelp.xml");
    }

    private void testOmXmlValiderer(String xmlFilNavn) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File xsdFil = Paths.get("src/main/resources/soknader/soknadstruktur.xsd").toFile();
        Schema schema = schemaFactory.newSchema(xsdFil);
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
