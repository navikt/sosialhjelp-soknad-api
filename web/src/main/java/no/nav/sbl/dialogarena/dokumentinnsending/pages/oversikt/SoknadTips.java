package no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt;

import no.nav.modig.content.CmsContentRetriever;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

public class SoknadTips extends Panel {
    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public SoknadTips(String id) {
        super(id);
        setOutputMarkupId(true);
        add(new Label("tittel", cmsContentRetriever.hentTekst("innsendingside.tips.tittel")));
        add(new Label("lukk", cmsContentRetriever.hentTekst("innsendingside.tips.lukk")));
        add(new Label("tips", cmsContentRetriever.hentTekst("innsendingside.tips.tekst")).setEscapeModelStrings(false));
    }
}
