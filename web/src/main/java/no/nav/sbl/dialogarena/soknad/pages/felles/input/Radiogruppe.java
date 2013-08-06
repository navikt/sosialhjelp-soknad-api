package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.pages.felles.input.inputkomponenter.Radioknapp;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;
import java.util.List;

public class Radiogruppe extends Panel {

    @Inject
    protected SoknadService soknadService;

    public Radiogruppe(String id, IModel<FaktumViewModel> model, IModel<List<FaktumViewModel>> statsborgerskapValg) {
        super(id, model);

        RadioGroup radiogruppe = new RadioGroup("radiogruppe", new Model<String>());
        radiogruppe.add(new ValgListe("valgliste", statsborgerskapValg));
        add(radiogruppe);

        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(soknadService, new PropertyModel<Faktum>(model, "faktum"), SaveInputBehavior.SAVE_ON_RADIOBUTTON_CHANGE) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onSelectionChanged(target);
            }
        };

        add(saveInputBehavior);
    }

    public void onSelectionChanged(AjaxRequestTarget target) {

    }

    private static class ValgListe extends ListView<FaktumViewModel> {

        public ValgListe(String id, IModel <List<FaktumViewModel>> model) {
            super(id, model);
        }

        @Override
        protected void populateItem(ListItem<FaktumViewModel> item) {
            item.add(new Radioknapp("element", item.getModel()));
        }
    }
}
