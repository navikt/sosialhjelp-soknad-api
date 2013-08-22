package no.nav.sbl.dialogarena.websoknad.behaviors;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveInputBehavior extends AbstractDefaultAjaxBehavior {

    public static final String SAVE_ON_CHANGE = "saveInputOnChange";
    public static final String SAVE_ON_RADIOBUTTON_CHANGE = "saveInputOnRadiobuttonChange";

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveInputBehavior.class);

    private WebSoknadService soknadService;
    private String jsFunctionName;

    public SaveInputBehavior(WebSoknadService soknadService) {
        this(soknadService, SAVE_ON_CHANGE);
    }

    public SaveInputBehavior(WebSoknadService soknadService, String jsFunctionName) {
        this.soknadService = soknadService;
        this.jsFunctionName = jsFunctionName;
    }

    @Override
    public final void renderHead(Component component, IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript(String.format(jsFunctionName + "(%s)", getJsonAsString())));
        super.renderHead(component, response);
    }

    @Override
    protected final void respond(AjaxRequestTarget target) {
        onAjaxCallback(target);
        String value = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("value").toString();
        IModel<Faktum> model = getFaktumModel();

        model.getObject().setValue(value);
        soknadService.lagreSoknadsFelt(model.getObject().getSoknadId(), model.getObject().getKey(), value);
    }

    private String getJsonAsString() {
        JSONObject json = new JSONObject();
        try {
            json.put("selector", getComponent().getMarkupId());
            json.put("callbackUrl", getCallbackUrl());
        } catch (JSONException e) {
            LOGGER.error("Kunne ikke opprette JSON objekt for timeoutboks");
        }
        return json.toString();
    }


    public void onAjaxCallback(AjaxRequestTarget target) {}

    private IModel<Faktum> getFaktumModel() {
        IModel model = getComponent().getDefaultModel();

        if (!(model instanceof CompoundPropertyModel)) {
            model = getComponent().getParent().getDefaultModel();
        }

        return new PropertyModel(model, "faktum");
    }
}
