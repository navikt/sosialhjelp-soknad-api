package no.nav.sbl.dialogarena.soknad.pages.startsoknad;

import no.nav.sbl.dialogarena.soknad.pages.opensoknad.OpenSoknadPage;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class StartSoknadPage extends WebPage {

    @Inject
    private SoknadService soknadService;

    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        String soknadGosysId = parameters.get("navSoknadId").toString();
        Long soknadId = soknadService.startSoknad(soknadGosysId);

        setResponsePage(OpenSoknadPage.class, new PageParameters().set("soknadId", soknadId));
    }
}
