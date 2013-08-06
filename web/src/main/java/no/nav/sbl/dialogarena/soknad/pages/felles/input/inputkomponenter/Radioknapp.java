package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class Radioknapp extends BaseInput {

    public Radioknapp(String id, IModel<String> labelModel) {
        this(id, labelModel, Model.of(false));
    }

    public Radioknapp(String id, IModel<String> labelModel, IModel inputModel) {
        super(id, labelModel, inputModel);
    }

    @Override
    protected Component addInputField(IModel inputModel) {
        Radio input = new Radio("input", inputModel);
        return input;
    }

    @Override
    protected Component addLabel(IModel labelModel) {
        WebMarkupContainer label = new WebMarkupContainer("label");
        label.add(new Label("labelTekst", labelModel));
        return label;
    }
}
