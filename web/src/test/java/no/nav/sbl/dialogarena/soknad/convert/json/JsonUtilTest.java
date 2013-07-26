package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonUtilTest {

    private JsonObject jsonObject;

    @Before
    public void setup() {
        jsonObject = new JsonObject();
    }

    @Test
    public void skalKonverterteJsonObjektTilListeAvJJsonInputElement() {
        jsonObject.addProperty("1", "1");
        jsonObject.addProperty("2", "2");
        jsonObject.addProperty("3", "3");

        List<JsonInputElement> list = JsonUtil.toList(jsonObject);

        assertThat(list.size(), is(3));
    }

    @Test
    public void skalKonverterteTomtJsonObjektTilTomListe() {
        List<JsonInputElement> list = JsonUtil.toList(jsonObject);

        assertThat(list.size(), is(0));
    }
}
