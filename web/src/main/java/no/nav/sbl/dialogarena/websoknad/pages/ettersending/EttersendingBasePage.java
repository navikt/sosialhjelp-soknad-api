package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendingService;
import no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class EttersendingBasePage extends BasePage {
    @Inject
    protected EttersendingService ettersendingService;

    public EttersendingBasePage(PageParameters parameters) {
        super(parameters);

        add(new SoknadComponent("soknad", SkjemaBootstrapFile.ETTERSENDING));
    }
}
