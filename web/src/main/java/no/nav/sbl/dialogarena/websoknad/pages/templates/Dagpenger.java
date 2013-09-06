package no.nav.sbl.dialogarena.websoknad.pages.templates;

import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.markup.html.WebPage;

import javax.inject.Inject;


public class Dagpenger extends WebPage {

    @Inject
    private WebSoknadService soknadService;

    public Dagpenger() {
//        Long soknadId = soknadService.startSoknad("dagpenger");
    }
}