package no.nav.sbl.dialogarena.dokumentinnsending.pages;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.service.BrukerBehandlingServiceIntegration;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class StartBehandlingPage extends WebPage {

    @Inject
    private BrukerBehandlingServiceIntegration brukerBehandlingServiceIntegration;

    public StartBehandlingPage(PageParameters pageParameters) {
        super(pageParameters);
        String userId = SubjectHandler.getSubjectHandler().getUid();
        String brukerbehandlingsId = BrukerBehandlingId.get(getPageParameters());
        brukerBehandlingServiceIntegration.oppdaterBrukerBehandling(brukerbehandlingsId, userId);
        getPageParameters().set("visInfo", true);
        setResponsePage(null, getPageParameters());
    }

}
