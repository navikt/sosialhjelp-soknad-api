package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;

import javax.inject.Inject;

public class StartSoknadPage extends BasePage {
    @Inject

    private SendSoknadService soknadService;

    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        add(new SoknadComponent("soknad"));
        StringValue brukerbehandlingId = getPageParameters().get("brukerbehandlingId");
        if (!brukerbehandlingId.isEmpty()) {
            new CookieUtils().remove("XSRF-TOKEN");
            new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadService.hentSoknadMedBehandlinsId(brukerbehandlingId.toString())));
        }
    }
}
