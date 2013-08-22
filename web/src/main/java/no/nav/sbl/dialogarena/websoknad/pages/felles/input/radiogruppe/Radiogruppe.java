package no.nav.sbl.dialogarena.websoknad.pages.felles.input.radiogruppe;

import no.nav.sbl.dialogarena.websoknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.websoknad.pages.felles.input.inputkomponenter.BaseInput;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import static no.nav.sbl.dialogarena.websoknad.behaviors.util.UtilBehaviors.labelFor;

public class Radiogruppe extends BaseInput {

    public Radiogruppe(String id) {
        super(id);

        RadioGroup radiogruppe = new RadioGroup("radiogruppe");
        radiogruppe.add(new ValgListe("valgliste"));
        radiogruppe.setRequired(true);
        add(radiogruppe);

        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(soknadService, SaveInputBehavior.SAVE_ON_RADIOBUTTON_CHANGE) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onSelectionChanged(target);
            }
        };

        add(saveInputBehavior);
    }

    private static class ValgListe extends ListView<String> {

        public ValgListe(String id) {
            super(id);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            Radio input = new Radio("value", new Model(item.getModel().getObject().toLowerCase()));
            WebMarkupContainer label = new WebMarkupContainer("labelContainer");
            label.add(labelFor(input.getMarkupId()));
            label.add(new Label("label", item.getModel()));
            item.add(label, input);
        }
    }

    public void onSelectionChanged(AjaxRequestTarget target) {}

    public IModel<Boolean> isSelected(final String value) {
        return new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                return ((RadiogruppeViewModel) getDefaultModelObject()).getRadiogruppe().equalsIgnoreCase(value);
            }
        };
    }
}
