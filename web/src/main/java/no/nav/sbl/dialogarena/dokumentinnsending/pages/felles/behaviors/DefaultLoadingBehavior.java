package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;

public class DefaultLoadingBehavior extends BaseLoadingBehavior {

    public DefaultLoadingBehavior() {
        this(Colour.HVIT, null);
    }

    public DefaultLoadingBehavior(Colour colour) {
        this(colour, null);
    }

    public DefaultLoadingBehavior(Colour colour, Component button) {
        super(colour, button);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        String markupId = getButtonMarkupId(component);
        addJavascript(String.format("addOnClickShowLoadingIndicator('%s', '%s');", markupId, markupId + INDICATOR_ID_POSTFIX));
        super.renderHead(component, response);
    }
}
