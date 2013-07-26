package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;

public class JsonInputElement extends JsonElement {

    private String key;

    public JsonInputElement(String key, JsonObject item) {
        super(item);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return getString("value");
    }

    public Boolean isVisible() {
        return getBoolean("visible");
    }

    public Boolean isModifiable() {
        return getBoolean("modifiable");
    }
}
