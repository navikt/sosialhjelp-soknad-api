package no.nav.sbl.dialogarena.dokumentinnsending.pages.base.modalbasepage;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.BasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.OpenPageLink;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class ModalBasePage extends BasePage {

    @Inject
    protected SoknadService soknadService;

    protected final String behandlingsId;

    public ModalBasePage(PageParameters parameters) {
        super(parameters);

        behandlingsId = BrukerBehandlingId.get(parameters);

        add(new OpenPageLink("close", null, behandlingsId));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (soknadService.hentSoknad(behandlingsId).status.er(SoknadStatus.UNDER_ARBEID)) {
            return;
        }
        throw new ApplicationException("Henvendelsen er avsluttet");
    }
}
