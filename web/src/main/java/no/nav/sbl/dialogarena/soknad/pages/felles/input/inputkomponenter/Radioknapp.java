package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class Radioknapp extends BaseInput {

    public Radioknapp(String id, IModel<FaktumViewModel> model) {
        super(id, model);
        add(cssClass("radioknapp"));
    }

    @Override
    protected Component addInputField() {
        Radio input = new Radio("value", Model.of(false));
        return input;
    }

    @Override
    protected Component addLabel() {
        WebMarkupContainer label = new WebMarkupContainer("labelContainer");
        label.add(new Label("label"));
        return label;
    }
}
