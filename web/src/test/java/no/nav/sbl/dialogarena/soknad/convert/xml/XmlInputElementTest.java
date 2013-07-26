package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.apache.wicket.util.file.File;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class XmlInputElementTest {

    private XmlInputElement xml;

    @Before
    public void setup() throws URISyntaxException, IOException {
        String path = "/xml/";
        File file = new File(getClass().getResource(path + "simpleNode.xml").toURI());
        XmlElement element = new XmlElement(file.readString());
        xml = new XmlInputElement(element.xml);
    }

    @Test
    public void getKeyReturnererNokkelen() {
        assertThat(xml.getKey(), is("key"));
    }

    @Test
    public void getValueReturnererVerdien() {
        assertThat(xml.getValue(), is("value"));
    }

    @Test
    public void isVisibleReturnererBoolean() {
        assertThat(xml.isVisible(), is(true));
    }

    @Test
    public void isModifiableReturnererBoolean() {
        assertThat(xml.isModifiable(), is(true));
    }
}
