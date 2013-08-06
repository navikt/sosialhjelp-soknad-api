package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;
import static no.nav.sbl.dialogarena.soknad.behaviors.util.UtilBehaviors.labelFor;

public abstract class BaseInput extends Panel {

    @Inject
    protected SoknadService soknadService;

    public BaseInput(String id, IModel<String> labelModel, IModel inputModel) {
        super(id);

        add(cssClass("input"));
        add(cssClass(id));

        Component label = addLabel(labelModel);

        Component input = addInputField(inputModel);
        label.add(labelFor(input.getMarkupId()));

        add(label, input);
    }

    protected abstract Component addInputField(IModel inputModel);

    protected Component addLabel(IModel labelModel) {
        return new Label("label", labelModel);
    }
}
