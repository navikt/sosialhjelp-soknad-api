package no.nav.sbl.dialogarena.websoknad.pages.startsoknad;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.SoknadComponent;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static no.nav.sbl.dialogarena.websoknad.pages.SkjemaBootstrapFile.DAGPENGER;

public class DagpengerAppPageClass extends BasePage {
    public DagpengerAppPageClass(PageParameters parameters) {
        super(parameters);
        WebComponent soknad;
        soknad = new SoknadComponent("soknad", DAGPENGER);
        soknad.add(new AttributeAppender("data-ng-app", "sendsoknad"));
        add(soknad);
    }
}
