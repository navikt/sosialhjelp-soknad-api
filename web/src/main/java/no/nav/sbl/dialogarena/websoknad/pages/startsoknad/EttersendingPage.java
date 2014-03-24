package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

public class EttersendingPage extends BasePage {
    @Inject

    private SendSoknadService soknadService;

    public EttersendingPage(PageParameters parameters) {
        super(parameters);
        add(new SoknadComponent("soknad"));

        StringValue brukerbehandlingId = getPageParameters().get("brukerbehandlingId");
        if (brukerbehandlingId.isEmpty()) {
            throw new ApplicationException("Kan ikke starte ettersending uten behandlingsID for en søknad");
        }

        Long soknadId = soknadService.hentEttersendingForBehandlingskjedeId(brukerbehandlingId.toString());
        if (soknadId != null) {
            new CookieUtils().remove("XSRF-TOKEN");
            new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId));
        }
    }
}
