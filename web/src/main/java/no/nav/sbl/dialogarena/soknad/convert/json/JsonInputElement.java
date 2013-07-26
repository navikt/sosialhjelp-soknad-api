package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;

public class JsonInputElement extends JsonElement {

    private static final String VALUE_KEY = "value";
    private static final String VISIBLE_KEY = "visible";
    private static final String MODIFIABLE_KEY = "modifiable";
    private String key;

    public JsonInputElement(String key, JsonObject item) {
        super(item);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return getString(VALUE_KEY);
    }

    public Boolean isVisible() {
        return getBoolean(VISIBLE_KEY);
    }

    public Boolean isModifiable() {
        return getBoolean(MODIFIABLE_KEY);
    }
}
