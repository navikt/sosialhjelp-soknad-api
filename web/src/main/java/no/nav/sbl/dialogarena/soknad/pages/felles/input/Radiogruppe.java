package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.Radioknapp;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import javax.inject.Inject;
import java.util.List;

public class Radiogruppe extends Panel {

    @Inject
    protected SoknadService soknadService;

    public Radiogruppe(String id, List<String> statsborgerskapValg, IModel soknadModel) {
        super(id);

        RadioGroup radiogruppe = new RadioGroup("radiogruppe", new Model<String>());
        radiogruppe.add(new ValgListe("valgliste", statsborgerskapValg));
        add(radiogruppe);

        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(soknadService, soknadModel, SaveInputBehavior.SAVE_ON_RADIOBUTTON_CHANGE) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onSelectionChanged(target);
            }
        };

        add(saveInputBehavior);
    }

    public void onSelectionChanged(AjaxRequestTarget target) {

    }

    private class ValgListe extends ListView<String> {

        public ValgListe(String id, List<String> list) {
            super(id, list);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            item.add(new Radioknapp("element", item.getModel()));
        }
    }
}
