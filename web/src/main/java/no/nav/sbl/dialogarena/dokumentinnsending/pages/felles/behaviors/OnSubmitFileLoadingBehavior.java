package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;

public class OnSubmitFileLoadingBehavior extends BaseLoadingBehavior {

    public OnSubmitFileLoadingBehavior(Component button) {
        this(Colour.HVIT, button);
    }

    public OnSubmitFileLoadingBehavior(Colour colour, Component button) {
        super(colour, button);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        String markupId = getButtonMarkupId(component);
        addJavascript(String.format("addShowLoadingIndicatorOnSubmitFile('%s', '%s');", markupId, markupId + INDICATOR_ID_POSTFIX));
        super.renderHead(component, response);
    }
}
