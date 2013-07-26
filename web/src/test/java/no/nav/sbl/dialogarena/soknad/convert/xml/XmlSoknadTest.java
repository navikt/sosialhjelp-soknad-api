package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.apache.wicket.util.file.File;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class XmlSoknadTest {

    private XmlSoknad xml;

    @Before
    public void setup() throws URISyntaxException, IOException {
        String path = "/xml/";
        File file = new File(getClass().getResource(path + "testsoknad.xml").toURI());
        xml = new XmlSoknad(file.readString());
    }

    @Test
    public void skalKunneHenteSoknadId() {
        assertThat(xml.getSoknadId(), is("1"));
    }

    @Test
    public void skalKunneHenteAlleInputNoder() {
        List<XmlInputElement> inputNodes = xml.getInputNodes();
        assertThat(inputNodes, notNullValue());
        assertThat(inputNodes.size(), is(4));
    }
}
