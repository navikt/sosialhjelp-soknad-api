package no.nav.sbl.dialogarena.websoknad.pages.opensoknad;

import no.nav.sbl.dialogarena.websoknad.common.PageEnum;
import no.nav.sbl.dialogarena.websoknad.common.SoknadId;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class OpenSoknadPage extends WebPage {

    @Inject
    private WebSoknadService soknadService;

    public OpenSoknadPage(PageParameters parameters) {
        super(parameters);
        Long soknadId = SoknadId.get(parameters);
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        Page page = PageEnum.getPage(soknad);
        setResponsePage(page);
    }
}
