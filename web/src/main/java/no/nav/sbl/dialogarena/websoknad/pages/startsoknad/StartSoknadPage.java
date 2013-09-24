package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.sendsoknad.SendSoknadServicePage;
import no.nav.sbl.dialogarena.websoknad.pages.soknad.OpenSoknadPage;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class StartSoknadPage extends BasePage {

    @Inject
    private WebSoknadService soknadService;

    Logger log = LoggerFactory.getLogger(SendSoknadServicePage.class);
    String soknadType;

    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        soknadType = parameters.get("soknadType").toString();
        log.debug("Starter soknad av type: " + soknadType);
        Long soknadId = soknadService.startSoknad(soknadType);
        PageParameters newParams = new PageParameters();
        newParams.add("soknadId", soknadId);
        setResponsePage(OpenSoknadPage.class, newParams);
    }

}
