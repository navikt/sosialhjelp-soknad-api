package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class TekstFelt<T> extends BaseInput {

    public TekstFelt(String id, IModel<String> labelModel, IModel<T> inputModel, IModel soknadModel) {
        super(id, labelModel, inputModel, soknadModel);
        add(cssClass("tekstfelt"));
    }

    @Override
    protected Component addInputField(IModel inputModel) {
        TextField<T> input = new TextField<T>("input", inputModel);
        return input;
    }
}
