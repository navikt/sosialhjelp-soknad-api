package no.nav.sbl.dialogarena.websoknad.pages.templates;

import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.apache.wicket.markup.html.WebPage;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: I140481
 * Date: 04.09.13
 * Time: 09:03
 * To change this template use File | Settings | File Templates.
 */
public class Dagpenger extends WebPage {

    @Inject
    private WebSoknadService soknadService;

    public Dagpenger(){

        Long soknadId = soknadService.startSoknad("dagpenger");
    }
}
