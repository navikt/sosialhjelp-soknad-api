package no.nav.sbl.dialogarena.websoknad.pages.utslagskriterier;

import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.beans.factory.annotation.Value;

public class UtslagskriterierDagpengerPage extends BasePage {

    public UtslagskriterierDagpengerPage(PageParameters parameters) {
        super(parameters);
        add(new SoknadComponent("soknad", SkjemaBootstrapFile.UTSLAGSKRITERIER_DAGPENGER));
    }
}
