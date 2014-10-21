package no.nav.sbl.dialogarena.websoknad.pages.mocksetup;

import no.nav.sbl.dialogarena.soknadinnsending.business.util.MockUtil;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.soknadliste.SoknadListePage;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.WildcardCollectionModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.MockUtil.VALGTMAANED_PROPERTY;

public class MockSetupPage extends BasePage {

    private ListView<MockSetupModel> listView;
    IModel<Integer> valgtMaaned;
    private static final List<String> MONTHS_LIST = Arrays.asList("Januar", "Februar", "Mars", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember");

    public MockSetupPage() {
        super(new PageParameters());
        add(
                new FeedbackPanel("feedback"),
                createVelgMockForm()
        );
    }

    @SuppressWarnings("unchecked")
    private Form<Void> createVelgMockForm() {
        listView = leggTilMockCheckBoxer();

        valgtMaaned = Model.of(0);
        Select<List<String>> months = new Select("months", valgtMaaned);
        IModel<Collection<? extends String>> monthModel = new WildcardCollectionModel<>(MONTHS_LIST);
        IOptionRenderer<String> renderer = new IOptionRenderer<String>() {
            @Override
            public String getDisplayValue(String object) {
                return object;
            }

            @Override
            public IModel getModel(String value) {
                return Model.of(MONTHS_LIST.indexOf(value));
            }
        };
        months.add(new SelectOptions("options", monthModel, renderer));

        return (Form<Void>) new Form<Void>("velgMockForm") {

            @Override
            protected void onSubmit() {
                List<MockSetupModel> models = listView.getModelObject();
                StringBuilder mocks = new StringBuilder();
                for (MockSetupModel model : models) {
                    setProperty(model.getKey(), model.getMockProperty());
                    mocks.append(model.getServiceName()).append(": ").append(getProperty(model.getKey())).append(", ");
                }
                info(mocks.toString());

                setProperty(VALGTMAANED_PROPERTY, valgtMaaned.getObject().toString());

                setResponsePage(SoknadListePage.class);
            }
        }.add(listView, months);
    }

    private ListView<MockSetupModel> leggTilMockCheckBoxer() {
        return new ListView<MockSetupModel>("radioliste", lagModeller()) {
            @Override
            protected void populateItem(ListItem<MockSetupModel> item) {
                item.add(
                        new Label("radioLabel", item.getModelObject().getServiceName()),
                        new CheckBox("radioValg", new PropertyModel<Boolean>(item.getModelObject(), "useMock"))
                );
            }
        };
    }

    private List<MockSetupModel> lagModeller() {
        return asList(
                new MockSetupModel("Startdato", MockUtil.TILLATSTARTDATOMOCK_PROPERTY)
        );
    }

}
