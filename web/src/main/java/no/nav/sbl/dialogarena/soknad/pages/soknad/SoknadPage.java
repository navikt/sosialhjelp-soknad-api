package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.soknad.convert.json.JsonInputElement;
import no.nav.sbl.dialogarena.soknad.convert.json.JsonSoknad;
import no.nav.sbl.dialogarena.soknad.convert.xml.XmlElement;
import no.nav.sbl.dialogarena.soknad.convert.xml.XmlParser;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.util.List;

public class SoknadPage extends BasePage {

    public SoknadPage(XmlParser xmlParser) {
        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                return new SoknadViewModel("Søknad");
            }
        }));
        add(new Label("soknadId", xmlParser.getSoknadId()));
        Form form = new Form("form");
        form.add(new XmlInputListe("inputList", xmlParser.getInputNodes()));
        add(form);
    }

    private static class XmlInputListe extends ListView<XmlElement> {

        public XmlInputListe(String id, List<XmlElement> xmlInputElements) {
            super(id, xmlInputElements);
        }

        @Override
        protected void populateItem(ListItem<XmlElement> item) {
            XmlElement element = item.getModelObject();
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


    public SoknadPage(JsonSoknad jsonParser) {
        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<SoknadViewModel>() {
            @Override
            protected SoknadViewModel load() {
                return new SoknadViewModel("Søknad");
            }
        }));
        add(new Label("soknadId", jsonParser.getSoknadId()));
        Form form = new Form("form");
        form.add(new JsonInputListe("inputList", jsonParser.getInputNodes()));
        add(form);
    }

    private static class JsonInputListe extends ListView<JsonInputElement> {

        public JsonInputListe(String id, List<JsonInputElement> jsonInputElements) {
            super(id, jsonInputElements);
        }

        @Override
        protected void populateItem(ListItem<JsonInputElement> item) {
            JsonInputElement element = item.getModelObject();
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
