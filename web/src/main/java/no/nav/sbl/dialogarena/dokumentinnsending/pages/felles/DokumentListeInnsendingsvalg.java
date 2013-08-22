package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.modig.wicket.model.ModelUtils;
import no.nav.modig.content.CmsContentRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.wicket.conditional.ConditionalUtils.visibleIf;

public class DokumentListeInnsendingsvalg extends GenericPanel<List<Dokument>> {

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    public DokumentListeInnsendingsvalg(String id, IModel<List<Dokument>> dokumenter, String tittelResource, final Boolean forhandsvis) {
        super(id);
        add(visibleIf(ModelUtils.not(ModelUtils.isEmptyList(dokumenter))));
        add(new Label("tittel", cmsContentRetriever.hentTekst(tittelResource)));
        ListView<Dokument> dokumentListe = new ListView<Dokument>("dokumenter", dokumenter) {
            @Override
            protected void populateItem(ListItem<Dokument> item) {
                final IModel<Dokument> dokument = item.getModel();
                if (forhandsvis) {
                    item.add(new DokumentNavnForhandsvisning("dokumentnavn", dokument));
                } else {
                    item.add(new Label("dokumentnavn", new PropertyModel<>(dokument, "navn")));
                }
            }
        };
        add(dokumentListe);
    }
}
