package no.nav.sbl.dialogarena.soknad.convert.xml;

import org.apache.wicket.util.file.File;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
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
        assertThat(xml.getSoknadId(), is(1L));
    }

    @Test
    public void skalKunneHenteAlleInputNoder() {
//        List<InputElement> inputNodes = xml.getInputNodes();
//        assertThat(inputNodes, notNullValue());
//        assertThat(inputNodes.size(), is(4));
    }
}
