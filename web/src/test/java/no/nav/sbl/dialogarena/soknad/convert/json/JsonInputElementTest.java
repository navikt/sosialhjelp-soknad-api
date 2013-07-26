package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class JsonInputElementTest {

    private JsonObject jsonObject;
    private static final String KEY = "key";

    @Before
    public void setup() {
        jsonObject = new JsonObject();
        jsonObject.addProperty("value", "value");
        jsonObject.addProperty("visible", true);
        jsonObject.addProperty("modifiable", true);
    }

    @Test
    public void skalOppretteJsonInputElement() {
        JsonInputElement jsonInputElement = new JsonInputElement(KEY, jsonObject);

        assertThat(jsonInputElement, notNullValue());
    }

    @Test
    public void skalHenteUtKey() {
        JsonInputElement jsonInputElement = new JsonInputElement(KEY, jsonObject);

        assertThat(jsonInputElement, notNullValue());
        assertThat(jsonInputElement.getKey(), is(KEY));
    }

    @Test
    public void skalHenteUtValue() {
        JsonInputElement jsonInputElement = new JsonInputElement(KEY, jsonObject);

        assertThat(jsonInputElement, notNullValue());
        assertThat(jsonInputElement.getValue(), is("value"));
    }

    @Test
    public void skalHenteUtVisible() {
        JsonInputElement jsonInputElement = new JsonInputElement(KEY, jsonObject);

        assertThat(jsonInputElement, notNullValue());
        assertThat(jsonInputElement.isVisible(), is(true));
    }

    @Test
    public void skalHenteUtModifiable() {
        JsonInputElement jsonInputElement = new JsonInputElement(KEY, jsonObject);

        assertThat(jsonInputElement, notNullValue());
        assertThat(jsonInputElement.isModifiable(), is(true));
    }
}
