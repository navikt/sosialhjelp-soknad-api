package no.nav.sbl.dialogarena.soknad.convert.json;

import org.apache.wicket.util.file.File;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class JsonSoknadTest {

    private String jsonString;

    @Before
    public void setup() throws URISyntaxException, IOException {
        String path = "/json/";
        File file = new File(getClass().getResource(path + "testsoknad.json").toURI());
        jsonString = file.readString();
    }

    @Test
    public void skalOppretteJsonSoknadFraString() {
        JsonSoknad jsonSoknad = new JsonSoknad(jsonString);

        assertThat(jsonSoknad, notNullValue());
    }

    @Test
    public void skalHenteSoknadsIdFraSoknad() {
        JsonSoknad jsonSoknad = new JsonSoknad(jsonString);

        assertThat(jsonSoknad, notNullValue());
        assertThat(jsonSoknad.getSoknadId(), is("2"));
    }

    @Test
    public void skalHenteListeMedInputElementFraSoknad() {
        JsonSoknad jsonSoknad = new JsonSoknad(jsonString);

        assertThat(jsonSoknad, notNullValue());
        List<JsonInputElement> inputElementList = jsonSoknad.getInputNodes();
        assertThat(inputElementList, notNullValue());
        assertThat(inputElementList.size(), is(4));
    }
}
