package no.nav.sbl.dialogarena.websoknad.pages.ettersending;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;

public class EttersendingPage extends EttersendingBasePage {

    public EttersendingPage(PageParameters parameters) {
        super(parameters);

        WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(brukerbehandlingId.toString());
        if (soknad != null) {
            new CookieUtils().remove("XSRF-TOKEN");
            new CookieUtils().save("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknad.getBehandlingskjedeId()));
        }
    }
}
