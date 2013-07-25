package no.nav.sbl.dialogarena.soknad.pages.felles.json;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.List;

public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

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
                    LOGGER.error("Kunne ikke opprette liste fra JSON", e);
                    throw new ApplicationException("Kunne ikke bygge opp søknaden", e);
                }
            }

            @Override
            public int size() {
                return jsonObject.length();
            }
        };
    }
}
