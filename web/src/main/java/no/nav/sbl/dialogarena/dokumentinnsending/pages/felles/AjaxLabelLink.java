package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;

public abstract class AjaxLabelLink extends AjaxLink {

    private IModel<String> label;

    public AjaxLabelLink(String id, IModel<String> label) {
        super(id);
        this.label = label;
    }

    public void onComponentTagBody(MarkupStream markupStream, ComponentTag componentTag) {
        replaceComponentTagBody(markupStream, componentTag, label.getObject().toString());
    }
}
