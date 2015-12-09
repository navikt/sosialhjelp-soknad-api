package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class SoknadXmlValiderer {

    private Validator validator;

    @Before
    public void setup() throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File("src/main/resources/soknader/soknadstruktur.xsd"));
        validator = schema.newValidator();
    }

    @Test
    public void testDagpengerXml() throws IOException, SAXException {
        test("dagpenger_ordinaer.xml");
    }

    @Test
    public void testForeldrepengerXml() throws IOException, SAXException {
        test("foreldresoknad.xml");
    }

    private void test(String xmlFilNavn) throws IOException, SAXException {
        String path = "src/main/resources/soknader/";
        validator.validate(new StreamSource(path + xmlFilNavn));
    }

}
