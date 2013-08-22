package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.sbl.dialogarena.dokumentinnsending.common.DokumentinnsendingParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

public class OpenPageLink extends Link<Void> {
    private final Class<? extends WebPage> target;
    private DokumentinnsendingParameters parameters;

    public OpenPageLink(String id, Class<? extends WebPage> target, String behandlingsId) {
        this(id, target, new DokumentinnsendingParameters(behandlingsId));
    }

    public OpenPageLink(String id, Class<? extends WebPage> target, DokumentinnsendingParameters parameters) {
        super(id);
        this.target = target;
        this.parameters = parameters;
    }

    @Override
    public void onClick() {
        setResponsePage(target, parameters);
    }
}
