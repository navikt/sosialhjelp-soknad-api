package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;
import static no.nav.sbl.dialogarena.soknad.behaviors.util.UtilBehaviors.labelFor;

public abstract class BaseInput extends Panel {

    @Inject
    protected SoknadService soknadService;

    public BaseInput(String id, IModel model) {
        super(id);
        setDefaultModel(new CompoundPropertyModel<FaktumViewModel>(new PropertyModel(model, id)));
        add(cssClass("input"));
        add(cssClass(id));

        Component label = addLabel();

        Component input = addInputField();
        label.add(labelFor(input.getMarkupId()));

        add(label, input);
    }

    public final SoknadService getSoknadService() {
        return soknadService;
    }

    protected abstract Component addInputField();

    protected abstract Component addLabel();
}
