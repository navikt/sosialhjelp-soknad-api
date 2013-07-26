package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonElement {

    protected JsonObject json;

    public JsonElement(String jsonString) {
        json = new JsonParser().parse(jsonString).getAsJsonObject();
    }

    public JsonElement(JsonObject item) {
        json = item;
    }

    protected String getString(String key) {
        try {
            return json.get(key).getAsString();
        } catch (NullPointerException e) {
            return "";
        }
    }

    protected Boolean getBoolean(String key) {
        try {
            return json.get(key).getAsBoolean();
        } catch (NullPointerException e) {
            return false;
        }
    }

    protected JsonObject getJsonObject(String key) {
        try {
            return json.get(key).getAsJsonObject();
        } catch (NullPointerException e) {
            return new JsonObject();
        }
    }
}
