package no.nav.sbl.dialogarena.soknad.pages.opensoknad;

import no.nav.sbl.dialogarena.soknad.common.PageEnum;
import no.nav.sbl.dialogarena.soknad.common.SoknadId;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;

public class OpenSoknadPage extends WebPage {

    @Inject
    private SoknadService soknadService;

    public OpenSoknadPage(PageParameters parameters) {
        super(parameters);
        Long soknadId = SoknadId.get(parameters);
        Soknad soknad = soknadService.hentSoknad(soknadId);
        Page page = PageEnum.getPage(soknad);
        setResponsePage(page);
    }
}
