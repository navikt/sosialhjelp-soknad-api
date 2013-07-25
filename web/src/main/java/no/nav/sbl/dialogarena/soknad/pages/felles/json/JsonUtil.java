package no.nav.sbl.dialogarena.soknad.pages.felles.json;

import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

import java.util.AbstractList;
import java.util.List;

public class JsonUtil {
    public static List<JsonElement> list(final JSONObject jsonObject) {
        return new AbstractList<JsonElement>() {
            @Override
            public JsonElement get(int index) {
                JSONArray names = jsonObject.names();
                try {
                    String key = names.getString(index);
                    JSONObject item = jsonObject.getJSONObject(key);
                    return new JsonElement(key, item);
                } catch (JSONException e) {
                    throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int size() {
                return jsonObject.length();
            }
        };
    }
}
