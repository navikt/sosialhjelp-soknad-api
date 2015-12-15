package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.XmlService;
import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class SoknadXmlValiderer {

    @Test
    public void testDagpengerXml() throws Exception {
        test("dagpenger/dagpenger_ordinaer.xml");
    }

    @Test
    public void testDagpengerGjenopptakXml() throws Exception {
        test("soknadtilleggsstonader.xml");
    }

    @Test
    public void testForeldrepengerXml() throws Exception {
        test("foreldresoknad.xml");
    }

    @Test
    public void testTilleggStonaderXml() throws Exception {
        test("soknadtilleggsstonader.xml");
    }

    @Test
    public void testRefusjonXml() throws Exception {
        test("refusjondagligreise.xml");
    }

    @Test
    public void testTiltakspengerXml() throws Exception {
        test("tiltakspenger.xml");
    }

    @Test
    public void testBilstonadXml() throws Exception {
        test("bilstonad.xml");
    }

    @Test
    public void testAapXml() throws Exception {
        test("aap_ordinaer.xml");
    }

    private void test(String xmlFilNavn) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(getClass().getClassLoader().getResourceAsStream("soknader/soknadstruktur.xsd")));
        Validator validator = schema.newValidator();

        StreamSource xmlSource = new XmlService().lastXmlFilMedInclude("soknader/" + xmlFilNavn);
        validator.validate(xmlSource);
    }

}
