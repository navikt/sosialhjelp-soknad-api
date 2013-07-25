package no.nav.sbl.dialogarena.soknad.pages.felles.json;

import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

public class JsonElement {

    private JSONObject json;
    private String key;

    public JsonElement(String key, JSONObject item) {
        json = item;
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

    private String getString(String jsonKey) {
        try {
            return json.getString(jsonKey);
        } catch (JSONException e) {
            return "";
        }
    }

    private Boolean getBoolean(String jsonKey) {
        try {
            return json.getBoolean(jsonKey);
        } catch (JSONException e) {
            return false;
        }
    }
}
