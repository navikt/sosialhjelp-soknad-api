package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class TekstFelt<T> extends BaseInput {

    private IModel soknadModel;

    public TekstFelt(String id, IModel<String> labelModel, IModel<T> inputModel, IModel soknadModel) {
        super(id, labelModel, inputModel);
        add(cssClass("tekstfelt"));
        this.soknadModel = soknadModel;
    }

    @Override
    protected Component addInputField(IModel inputModel) {
        TextField<T> input = new TextField<T>("input", inputModel);
        input.add(new SaveInputBehavior(soknadService, soknadModel));
        return input;
    }
}
