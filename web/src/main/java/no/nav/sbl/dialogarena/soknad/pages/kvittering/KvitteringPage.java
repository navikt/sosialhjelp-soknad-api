package no.nav.sbl.dialogarena.soknad.pages.kvittering;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class KvitteringPage extends BasePage {

    public KvitteringPage(final Soknad soknad) {
        super();

        CompoundPropertyModel<KvitteringViewModel> pageModel = new CompoundPropertyModel<>(new LoadableDetachableModel<KvitteringViewModel>() {
            @Override
            protected KvitteringViewModel load() {
                return new KvitteringViewModel("Kvittering", soknad);
            }
        });
        setDefaultModel(pageModel);

        add(new Label("kvittering", "Du har sendt inn søknaden. Snart får du penger!"));
    }
}
