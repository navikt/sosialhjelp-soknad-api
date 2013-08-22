package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextValidationJSBehavior extends Behavior {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    private Map<String, ArrayList<Object>> propertyMap = new LinkedHashMap<>();
    private boolean removeOnFocus = true;

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript(String.format("inlineValidation('%s', %s, %b);", component.getMarkupId(), getJsonString(), removeOnFocus)));
        super.renderHead(component, response);
    }

    public TextValidationJSBehavior setMaxLength(int maxLength, String errorKey) {
        addToHashMap("maxLength", maxLength, errorKey);
        return this;
    }

    public TextValidationJSBehavior setPattern(String pattern, String errorKey) {
        addToHashMap("pattern", pattern, errorKey);
        return this;
    }

    public TextValidationJSBehavior setRemoveOnFocus(boolean removeOnFocus) {
        this.removeOnFocus = removeOnFocus;
        return this;
    }

    public TextValidationJSBehavior setRequired(boolean required, String errorKey) {
        addToHashMap("required", required, errorKey);
        return this;
    }

    private void addToHashMap(String key, Object value, String errorKey) {
        ArrayList<Object> values = new ArrayList<>();
        values.add(value);
        values.add(getApplicationProperty(errorKey));
        propertyMap.put(key, values);
    }

    public String getJsonString() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, ArrayList<Object>> entry : propertyMap.entrySet()) {
            setJsonObject(json, entry.getKey(), entry.getValue());
        }
        return json.toString();
    }

    private void setJsonObject(JSONObject jsonObject, String key, List<Object> values) {
        JSONObject options = new JSONObject();
        try {
            options.put("value", values.get(0));
            options.put("error", values.get(1));
            jsonObject.put(key, options);
        } catch (JSONException e) {
            throw new ApplicationException("Kunne ikke generere JSON objekt for tooltip", e);
        }
    }

    protected String getApplicationProperty(String key) {
        return cmsContentRetriever.hentTekst(key);
    }
}