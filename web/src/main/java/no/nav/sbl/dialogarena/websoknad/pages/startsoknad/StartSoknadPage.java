package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.DAGPENGER;
import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.GJENOPPTAK;

public class StartSoknadPage extends BasePage {
    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        SkjemaBootstrapFile skjematype = parameters.get("skjemanummer").toString().equals("NAV04-01.03") ? DAGPENGER : GJENOPPTAK;
        add(new SoknadComponent("soknad", skjematype));
    }
}
