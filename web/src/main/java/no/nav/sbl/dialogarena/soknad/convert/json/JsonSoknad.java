package no.nav.sbl.dialogarena.soknad.convert.json;

import java.util.List;

public class JsonSoknad extends JsonElement {

    public JsonSoknad(String jsonString) {
        super(jsonString);
        json = json.get("soknad").getAsJsonObject();
    }

    public String getSoknadId() {
        return getString("soknadId");
    }

    public List<JsonInputElement> getInputNodes() {
        return JsonUtil.toList(getJsonObject("inputFields"));
    }
}
