package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class JsonElementTest {

    private JsonObject jsonObject = new JsonObject();

    @Test
     public void skalOppretteJsonElementFraJsonObjekt() {
        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
    }

    @Test
    public void skalOppretteJsonElementFraJsonString() {
        String testKey = "key";
        String testValue = "value";
        String jsonString = "{\"" + testKey + "\": \"" + testValue + "\"}";
        JsonElement element = new JsonElement(jsonString);

        assertThat(element, notNullValue());
        assertThat(element.getString(testKey), is(testValue));
    }

    @Test
    public void skalKunneHenteStringFraJsonElement() {
        String testKey = "key";
        String testValue = "value";
        jsonObject.addProperty(testKey, testValue);

        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getString(testKey), is(testValue));
    }

    @Test
    public void skalKunneHenteBooleanFraJsonElement() {
        String testKey = "key";
        Boolean testValue = true;
        jsonObject.addProperty(testKey, testValue);

        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getBoolean(testKey), is(testValue));
    }

    @Test
    public void skalKunneHenteJsonObjektFraJsonElement() {
        String testKey = "key";
        JsonObject testValue = new JsonObject();
        jsonObject.add(testKey, testValue);

        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getJsonObject(testKey), is(testValue));
    }

    @Test
    public void getStringSkalReturnereEnTomStringDersomNokkelenIkkeEksisterer() {
        String testKey = "key";
        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getString(testKey), isEmptyString());
    }

    @Test
    public void getBooleanSkalReturnereFalseDersomNokkelenIkkeEksisterer() {
        String testKey = "key";
        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getBoolean(testKey), is(false));
    }

    @Test
    public void getJsonObjectSkalReturnereEttTomtJsonObjektDersomNokkelenIkkeEksisterer() {
        String testKey = "key";
        JsonElement element = new JsonElement(jsonObject);

        assertThat(element, notNullValue());
        assertThat(element.getJsonObject(testKey), notNullValue());
        assertThat(element.getJsonObject(testKey).entrySet().size(), is(0));
    }
}
