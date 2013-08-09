package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import static no.nav.sbl.dialogarena.soknad.behaviors.util.UtilBehaviors.labelFor;

public class Checkboks extends BaseInput {

    public Checkboks(String id) {
        super(id);

        CheckBox input = new CheckBox("booleanValue");
        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(soknadService) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onToggle(target);
            }
        };
        input.add(saveInputBehavior);

        WebMarkupContainer label = new WebMarkupContainer("labelContainer");
        label.add(new Label("label"));
        label.add(labelFor(input.getMarkupId()));
        add(input, label);
    }

    public void onToggle(AjaxRequestTarget target) {}

    public IModel<Boolean> isChecked() {
        return new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                return ((FaktumViewModel) getDefaultModelObject()).getBooleanValue();
            }
        };
    }
}
