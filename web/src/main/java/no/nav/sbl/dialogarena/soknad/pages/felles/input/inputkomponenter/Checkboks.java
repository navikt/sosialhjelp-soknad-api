package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class Checkboks extends BaseInput{

    private IModel soknadModel;

    public Checkboks(String id, IModel<String> labelModel, IModel soknadModel) {
        this(id, labelModel, Model.of(false), soknadModel);
    }

    public Checkboks(String id, IModel<String> labelModel, IModel inputModel, IModel soknadModel) {
        super(id, labelModel, inputModel);
        add(cssClass("checkboks"));
        this.soknadModel = soknadModel;
    }

    @Override
    protected Component addInputField(IModel inputModel) {
        CheckBox input = new CheckBox("input", inputModel);
        input.add(new SaveInputBehavior(soknadService, soknadModel));
        return input;
    }

    @Override
    protected Component addLabel(IModel labelModel) {
        WebMarkupContainer label = new WebMarkupContainer("label");
        label.add(new Label("labelTekst", labelModel));
        return label;
    }
}
