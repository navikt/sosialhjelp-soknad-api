package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.apache.wicket.util.file.File;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class XmlElementTest {

    @Test
    public void skalOppretteXmlElementFraXmlString() throws URISyntaxException, IOException {
        String path = "/xml/";
        File file = new File(getClass().getResource(path + "simpleString.xml").toURI());
        String xmlString = file.readString();

        XmlElement element = new XmlElement(xmlString);

        assertThat(element, notNullValue());
        assertThat(element.getString("key"), is("value"));
    }

    @Test
    public void skalKunneHenteBooleanFraXml() throws URISyntaxException, IOException {
        String path = "/xml/";
        File file = new File(getClass().getResource(path + "simpleBoolean.xml").toURI());
        String xmlString = file.readString();

        XmlElement element = new XmlElement(xmlString);

        assertThat(element, notNullValue());
        assertThat(element.getBoolean("key"), is(true));
    }
}
