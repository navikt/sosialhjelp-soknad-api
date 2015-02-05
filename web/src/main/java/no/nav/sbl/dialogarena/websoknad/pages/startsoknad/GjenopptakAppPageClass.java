package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.GJENOPPTAK;

public class GjenopptakAppPageClass extends BasePage {
    public GjenopptakAppPageClass(PageParameters parameters) {
        super(parameters);
        WebComponent soknad;
        soknad = new SoknadComponent("soknad", GJENOPPTAK);
        soknad.add(new AttributeAppender("data-ng-app", "gjenopptak"));
        add(soknad);
    }
}
