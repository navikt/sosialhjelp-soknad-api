package no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.FaktumViewModel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import static no.nav.modig.wicket.shortcuts.Shortcuts.cssClass;

public class TekstFelt extends BaseInput {

    public TekstFelt(String id, IModel<FaktumViewModel> model) {
        super(id, model);
        add(cssClass("tekstfelt"));
    }

    @Override
    protected Component addInputField() {
        TextField<String> input = new TextField<>("value");
        input.add(new SaveInputBehavior(soknadService, new PropertyModel<Faktum>(getDefaultModel(), "faktum")));
        return input;
    }
}
