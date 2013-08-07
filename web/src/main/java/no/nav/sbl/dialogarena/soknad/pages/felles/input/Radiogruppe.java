package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import no.nav.sbl.dialogarena.soknad.behaviors.SaveInputBehavior;
import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.lang.collections.PredicateUtils.equalToIgnoreCase;
import static no.nav.modig.wicket.model.ModelUtils.when;
import static no.nav.sbl.dialogarena.soknad.behaviors.util.UtilBehaviors.labelFor;

public class Radiogruppe extends Panel {

    @Inject
    private SoknadService soknadService;

    private IModel selected;

    public Radiogruppe(String id, final IModel model, IModel<List<FaktumViewModel>> statsborgerskapValg) {
        super(id);
        CompoundPropertyModel<FaktumViewModel> defaultModel = new CompoundPropertyModel<FaktumViewModel>(new PropertyModel(model, id));
        setDefaultModel(defaultModel);

        selected = new PropertyModel(defaultModel, "value");
        RadioGroup radiogruppe = new RadioGroup("radiogruppe", selected);
        radiogruppe.add(new ValgListe("valgliste", statsborgerskapValg));
        add(radiogruppe);

        SaveInputBehavior saveInputBehavior = new SaveInputBehavior(soknadService, new PropertyModel<Faktum>(defaultModel, "faktum"), SaveInputBehavior.SAVE_ON_RADIOBUTTON_CHANGE) {
            @Override
            public void onAjaxCallback(AjaxRequestTarget target) {
                onSelectionChanged(target);
            }
        };

        add(saveInputBehavior);
    }

    private static class ValgListe extends ListView<FaktumViewModel> {

        public ValgListe(String id, IModel<List<FaktumViewModel>> model) {
            super(id, model);
        }

        @Override
        protected void populateItem(ListItem<FaktumViewModel> item) {
            CompoundPropertyModel<FaktumViewModel> model = new CompoundPropertyModel<>(item.getModel());

            item.setDefaultModel(model);
            Radio input = new Radio("value", new PropertyModel(model, "labelLowerCase"));
            WebMarkupContainer label = new WebMarkupContainer("labelContainer");
            label.add(labelFor(input.getMarkupId()));
            label.add(new Label("label"));

            item.add(label, input);
        }
    }

    public void onSelectionChanged(AjaxRequestTarget target) {}

    public IModel<Boolean> isSelected(String value) {
        return when(selected, equalToIgnoreCase(value));
    }
}
