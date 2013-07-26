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
        if (json.has(key)) {
            return json.get(key).getAsString();
        }
        return "";
    }

    protected Boolean getBoolean(String key) {
        if (json.has(key)) {
            return json.get(key).getAsBoolean();
        }
        return false;
    }

    protected JsonObject getJsonObject(String key) {
        if (json.has(key)) {
            return json.get(key).getAsJsonObject();
        }
        return new JsonObject();
    }
}
