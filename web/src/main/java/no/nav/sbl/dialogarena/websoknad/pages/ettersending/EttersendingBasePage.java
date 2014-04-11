package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

public class EttersendingBasePage extends BasePage {
    @Inject
    protected SendSoknadService soknadService;

    protected StringValue brukerbehandlingId;

    public EttersendingBasePage(PageParameters parameters) {
        super(parameters);

        add(new SoknadComponent("soknad", true));

        brukerbehandlingId = parameters.get("brukerbehandlingId");
        if (brukerbehandlingId.isEmpty()) {
            throw new ApplicationException("Kan ikke starte ettersending uten behandlingsID for en søknad");
        }
    }
}
