package no.nav.sbl.dialogarena.websoknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.websoknad.behaviors.SaveInputBehavior;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;

import static no.nav.sbl.dialogarena.websoknad.behaviors.util.UtilBehaviors.labelFor;

public class TekstFelt extends BaseInput {

    public TekstFelt(String id) {
        super(id);

        TextField<String> input = new TextField<>("value");
        input.add(new SaveInputBehavior(soknadService));

        Label label = new Label("label");
        label.add(labelFor(input.getMarkupId()));

        add(label, input);
    }
}