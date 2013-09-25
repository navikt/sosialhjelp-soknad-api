package no.nav.sbl.dialogarena.websoknad.pages.soknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

/**
 * Denne siden laster en søknad inn i siden.
 */
public class OpenSoknadPage extends BasePage {
    @Inject
    public WebSoknadService service;

    public OpenSoknadPage(PageParameters parameters) {
        super(parameters);
        Long soknadId = parameters.get("soknadId").toLongObject();
        add(new SoknadComponent("soknad", service.hentSoknad(soknadId)));
    }


}
