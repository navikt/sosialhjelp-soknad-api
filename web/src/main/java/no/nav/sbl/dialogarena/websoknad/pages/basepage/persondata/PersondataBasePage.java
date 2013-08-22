package no.nav.sbl.dialogarena.websoknad.pages.basepage.persondata;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class PersondataBasePage extends BasePage {

    public PersondataBasePage(final WebSoknad soknad) {
        super();
        CompoundPropertyModel<PersondataViewModel> pageModel = new CompoundPropertyModel<>(new LoadableDetachableModel<PersondataViewModel>() {
            @Override
            protected PersondataViewModel load() {
                return new PersondataViewModel("Søknad", soknad);
            }
        });
        setDefaultModel(pageModel);

        add(new Label("navnLabel"));
        add(new Label("fornavn"));
        add(new Label("etternavn"));

        add(new Label("fnrLabel"));
        add(new Label("fnr"));

        add(new Label("adresseLabel"));
        add(new Label("adresse"));
        add(new Label("postnr"));
        add(new Label("poststed"));
    }
}
