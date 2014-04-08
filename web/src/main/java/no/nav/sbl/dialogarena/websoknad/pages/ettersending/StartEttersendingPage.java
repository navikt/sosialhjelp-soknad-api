package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class StartEttersendingPage extends EttersendingBasePage {
    public StartEttersendingPage(PageParameters parameters) {
        super(parameters);
        add(new SoknadComponent("soknad"));
    }
}
