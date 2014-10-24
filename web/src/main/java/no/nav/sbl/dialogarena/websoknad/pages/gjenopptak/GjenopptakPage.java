package no.nav.sbl.dialogarena.websoknad.pages.gjenopptak;

import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class GjenopptakPage extends BasePage {

    public GjenopptakPage(PageParameters parameters) {
        super(parameters);

        add(new SoknadComponent("soknad", SkjemaBootstrapFile.GJENOPPTAK));
    }
}
