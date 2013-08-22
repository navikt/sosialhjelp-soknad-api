package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.behaviors.JSPreviewBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;

public class DokumentNavnForhandsvisning extends Panel {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public DokumentNavnForhandsvisning(String id, IModel<Dokument> dokument) {
        super(id);
        add(new Label("dokumentnavn", new PropertyModel(dokument, "navn")));
        Label forhandsvis = new Label("forhandsvis", cmsContentRetriever.hentTekst("bekreftelsesside.forhandsvisning"));
        forhandsvis.add(new JSPreviewBehavior(dokument));
        add(forhandsvis);
    }
}
