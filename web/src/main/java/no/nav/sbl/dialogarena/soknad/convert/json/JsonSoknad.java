package no.nav.sbl.dialogarena.soknad.convert.json;

import no.nav.sbl.dialogarena.soknad.convert.InputElement;
import no.nav.sbl.dialogarena.soknad.convert.Soknad;

import java.util.List;

public class JsonSoknad extends JsonElement implements Soknad {

    public JsonSoknad(String jsonString) {
        super(jsonString);
        json = json.get("soknad").getAsJsonObject();
    }

    public String getSoknadId() {
        return getString("soknadId");
    }

    public List<InputElement> getInputNodes() {
        return JsonUtil.toList(getJsonObject("inputFields"));
    }
}
