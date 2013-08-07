package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class TekstFelt extends BaseInput {

    public TekstFelt(String id, IModel model) {
        super(id, model);
        add(cssClass("tekstfelt"));
    }

    @Override
    protected final Component addInputField() {
        TextField<String> input = new TextField<>("value");
        input.add(new SaveInputBehavior(getSoknadService(), new PropertyModel<Faktum>(getDefaultModel(), "faktum")));
        return input;
    }

    @Override
    protected final Component addLabel() {
        return new Label("label");
    }
}