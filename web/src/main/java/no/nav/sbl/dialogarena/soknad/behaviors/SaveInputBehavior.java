package no.nav.sbl.dialogarena.soknad.behaviors;

import no.nav.sbl.dialogarena.soknad.convert.xml.XmlSoknad;
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

    private static final Logger logger = LoggerFactory.getLogger(SaveInputBehavior.class);
    private SoknadService soknadService;
    private IModel<XmlSoknad> soknad;
    private String key;

    public SaveInputBehavior(SoknadService soknadService, IModel<XmlSoknad> soknad, String key) {
        this.soknadService = soknadService;
        this.soknad = soknad;
        this.key = key;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript(String.format("saveInputOnFocusOut(%s)", getJsonAsString())));
        super.renderHead(component, response);
    }

    @Override
    protected void respond(AjaxRequestTarget target) {
        if (key != null) {
            String value = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("value").toString();
            soknadService.lagreSoknadsFelt(soknad.getObject().getSoknadId(), key, value);
        } else {
            logger.error("Prøver å sette brukerdata med nøkkel som er lik null");
        }
    }

    private String getJsonAsString() {
        JSONObject json = new JSONObject();
        try {
            json.put("componentId", getComponent().getMarkupId());
            json.put("callbackUrl", getCallbackUrl());
        } catch (JSONException e) {
            logger.error("Kunne ikke opprette JSON objekt for timeoutboks");
        }
        return json.toString();
    }
}
