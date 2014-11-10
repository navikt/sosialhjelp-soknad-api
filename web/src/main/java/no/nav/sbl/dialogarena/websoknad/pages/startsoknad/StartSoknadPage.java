package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.DAGPENGER;
import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.GJENOPPTAK;

public class StartSoknadPage extends BasePage {
    public StartSoknadPage(PageParameters parameters) {
        super(parameters);
        WebComponent soknad;
        switch(parameters.get("skjemanummer").toString()) {
            case "NAV04-01.03":
                soknad = new SoknadComponent("soknad", DAGPENGER);
                soknad.add(new AttributeAppender("data-ng-app", "sendsoknad"));
                add(soknad);
                break;
            case "NAV04-16.03":
                soknad = new SoknadComponent("soknad", GJENOPPTAK);
                soknad.add(new AttributeAppender("data-ng-app", "gjenopptak"));
                add(soknad);
                break;
            default:
                add(new FeilsidePanel("soknad"));
                break;
        }
    }
}
