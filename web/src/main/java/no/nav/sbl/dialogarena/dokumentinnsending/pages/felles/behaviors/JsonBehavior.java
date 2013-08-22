package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonBehavior extends Behavior {
    private static final Logger logger = LoggerFactory.getLogger(JsonBehavior.class);

    protected void setJsonObject(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            String error = String.format("Kunne ikke legge til %s med key %s i JSON", value.toString(), key);
            logger.error(error);
            throw new ApplicationException("Noe gikk galt i renderingen av siden", e);
        }
    }
}
