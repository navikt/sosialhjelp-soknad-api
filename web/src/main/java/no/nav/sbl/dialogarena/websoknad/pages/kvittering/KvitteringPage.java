package no.nav.sbl.dialogarena.websoknad.pages.kvittering;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class KvitteringPage extends BasePage {

    public KvitteringPage(final WebSoknad soknad) {
        super();

        CompoundPropertyModel<KvitteringViewModel> pageModel = new CompoundPropertyModel<>(new LoadableDetachableModel<KvitteringViewModel>() {
            @Override
            protected KvitteringViewModel load() {
                return new KvitteringViewModel("Kvittering", soknad);
            }
        });
        setDefaultModel(pageModel);
    }
}
