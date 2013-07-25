package no.nav.sbl.dialogarena.soknad.pages.felles.json;

import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    private JSONObject json;

    public JsonParser(String jsonString) {
        try {
            json = new JSONObject(jsonString).getJSONObject("soknad");
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
