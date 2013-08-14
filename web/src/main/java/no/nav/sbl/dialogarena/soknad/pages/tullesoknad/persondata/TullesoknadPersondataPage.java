package no.nav.sbl.dialogarena.soknad.pages.tullesoknad.persondata;


import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.persondata.PersondataBasePage;
import no.nav.sbl.dialogarena.soknad.pages.tullesoknad.SoknadPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

public class TullesoknadPersondataPage extends PersondataBasePage {
    public TullesoknadPersondataPage(final Soknad soknad) {
        super(soknad);

        Link neste = new Link("neste") {
            @Override
            public void onClick() {
                setResponsePage(new SoknadPage(soknad));
            }
        };
        neste.setBody(Model.of("Neste"));
        add(neste);
    }
}
