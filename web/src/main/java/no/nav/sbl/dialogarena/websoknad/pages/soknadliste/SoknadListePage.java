package no.nav.sbl.dialogarena.websoknad.pages.soknadliste;

import no.nav.sbl.dialogarena.websoknad.pages.basepage.BasePage;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SoknadListePage extends BasePage {

	public SoknadListePage() {
        super(new PageParameters());

        add(new Link<String>("dagpenger") {
            @Override
            public void onClick() {
                PageParameters parameters = new PageParameters().set("soknadType", "Dagpenger");
                setResponsePage(StartSoknadPage.class, parameters);
            }
        });
	}


}
