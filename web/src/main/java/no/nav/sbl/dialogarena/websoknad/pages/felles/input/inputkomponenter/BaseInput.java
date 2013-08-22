package no.nav.sbl.dialogarena.websoknad.pages.felles.input.inputkomponenter;

import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import javax.inject.Inject;

public abstract class BaseInput extends Panel {

    @Inject
    protected WebSoknadService soknadService;

    public BaseInput(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        IModel defaultModel = getDefaultModel();
        if (!(defaultModel instanceof CompoundPropertyModel)) {
            setDefaultModel(new Model());
            setDefaultModel(new CompoundPropertyModel(defaultModel));
        }
    }
}
