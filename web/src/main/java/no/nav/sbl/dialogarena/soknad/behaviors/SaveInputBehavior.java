package no.nav.sbl.dialogarena.soknad.behaviors;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveInputBehavior extends AbstractDefaultAjaxBehavior {

    public static final String SAVE_ON_CHANGE = "saveInputOnChange";
    public static final String SAVE_ON_RADIOBUTTON_CHANGE = "saveInputOnRadiobuttonChange";

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveInputBehavior.class);
    private SoknadService soknadService;
    private IModel<Faktum> faktum;
    private String jsFunctionName;

    public SaveInputBehavior(SoknadService soknadService, IModel<Faktum> faktum) {
        this(soknadService, faktum, SAVE_ON_CHANGE);
    }

    public SaveInputBehavior(SoknadService soknadService, IModel<Faktum> faktum, String jsFunctionName) {
        this.soknadService = soknadService;
        this.faktum = faktum;
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
        soknadService.lagreSoknadsFelt(faktum.getObject().getSoknadId(), faktum.getObject().getKey(), value);
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


    public void onAjaxCallback(AjaxRequestTarget target) {

    }
}
