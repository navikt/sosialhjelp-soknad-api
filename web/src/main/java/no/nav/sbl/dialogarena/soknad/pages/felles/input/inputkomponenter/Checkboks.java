package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class Checkboks extends BaseInput{

    public Checkboks(String id, IModel<FaktumViewModel> model) {
        super(id, model);
        add(cssClass("checkboks"));
    }

    @Override
    protected final Component addInputField() {
        CheckBox input = new CheckBox("value");
        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(getSoknadService(), new PropertyModel<Faktum>(getDefaultModel(), "faktum")) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onToggle(target);
            }
        };
        input.add(saveInputBehavior);
        return input;
    }

    @Override
    protected final Component addLabel() {
        WebMarkupContainer label = new WebMarkupContainer("labelContainer");
        label.add(new Label("label"));
        return label;
    }

    public void onToggle(AjaxRequestTarget target) {}
}