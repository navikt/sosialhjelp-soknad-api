package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonObject;
import no.nav.sbl.dialogarena.soknad.convert.InputElement;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonInputElement extends JsonElement implements InputElement{

    private static final String VALUE_KEY = "value";
    private static final String VISIBLE_KEY = "visible";
    private static final String MODIFIABLE_KEY = "modifiable";
    private static final String TYPE_KEY = "type";
    private static final String DEFAULT_TYPE = "text";

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

    public String getType() {
        String type = getString(TYPE_KEY);

        if (isBlank(type)) {
            type = DEFAULT_TYPE;
        }
        return type;
    }

    public Boolean isVisible() {
        return getBoolean(VISIBLE_KEY);
    }

    public Boolean isModifiable() {
        return getBoolean(MODIFIABLE_KEY);
    }
}
