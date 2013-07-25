package no.nav.sbl.dialogarena.soknad.pages.felles.json;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);
    private JSONObject json;

    public JsonParser(String jsonString) {
        try {
            json = new JSONObject(jsonString).getJSONObject("soknad");
        } catch (JSONException e) {
            LOGGER.error("Kunne ikke opprette JSON-objekt", e);
            throw new ApplicationException("Kunne ikke bygge søknaden", e);
        }
    }

    public String getSoknadId() {
        try {
            return json.getString("soknadId");
        } catch (JSONException e) {
            return "";
        }
    }

    public List<JsonElement> getInputNodes() {
        try {
            return JsonUtil.list(json.getJSONObject("inputFields"));
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }
}
