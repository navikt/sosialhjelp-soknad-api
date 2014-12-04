package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import no.nav.modig.core.exception.ApplicationException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

public class StartEttersendingPage extends EttersendingBasePage {
    public StartEttersendingPage(PageParameters parameters) {
        super(parameters);

        StringValue brukerbehandlingId = parameters.get("brukerbehandlingId");
        if (brukerbehandlingId.isEmpty()) {
            throw new ApplicationException("Kan ikke starte ettersending uten behandlingsID for en søknad");
        }
    }
}
