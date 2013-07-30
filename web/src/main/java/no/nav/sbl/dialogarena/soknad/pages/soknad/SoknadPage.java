package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.common.SoknadId;
import no.nav.sbl.dialogarena.soknad.convert.InputElement;
import no.nav.sbl.dialogarena.soknad.convert.Soknad;
import no.nav.sbl.dialogarena.soknad.convert.json.JsonSoknad;
import no.nav.sbl.dialogarena.soknad.convert.xml.XmlSoknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class SoknadPage extends BasePage {

    @Inject
    private SoknadService soknadService;

    public SoknadPage(PageParameters parameters) {
        super(parameters);

        final Long soknadId = SoknadId.get(parameters);

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                Soknad soknad;
                if (soknadId == 1L) {
                    soknad = new XmlSoknad(soknadService.hentSoknad(soknadId));
                } else {
                    soknad = new JsonSoknad(soknadService.hentSoknad(soknadId));
                }
                return new SoknadViewModel("Søknad", soknad);
            }
        }));
        add(new Label("soknadId", soknadId));
        Form form = new Form("form");
        form.add(new InputListe("inputList"));
        add(form);
    }


    private static class InputListe extends ListView<InputElement> {

        public InputListe(String id) {
            super(id);
        }

        @Override
        protected void populateItem(ListItem<InputElement> item) {
            InputElement element = item.getModelObject();
            TextField<String> textField = new TextField<>("input", Model.of(element.getValue()));
            textField.setVisible(element.isVisible());
            textField.setEnabled(element.isModifiable());
            textField.setOutputMarkupId(true);
            item.add(textField);

            Label label = new Label("label", element.getKey());
            label.add(new AttributeAppender("for", textField.getMarkupId()));
            label.setVisible(element.isVisible());
            item.add(label);
        }
    }
}
